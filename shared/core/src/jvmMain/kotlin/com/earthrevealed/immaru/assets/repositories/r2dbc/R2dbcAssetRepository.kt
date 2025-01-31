package com.earthrevealed.immaru.assets.repositories.r2dbc

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.DeleteAssetException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.assets.SaveAssetException
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.AuditFields
import com.earthrevealed.immaru.r2dbc.bindNullable
import com.earthrevealed.immaru.r2dbc.getByteArray
import com.earthrevealed.immaru.r2dbc.getString
import com.earthrevealed.immaru.r2dbc.getTimestamp
import com.earthrevealed.immaru.r2dbc.getUuid
import com.earthrevealed.immaru.r2dbc.useConnection
import com.earthrevealed.immaru.r2dbc.useTransaction
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.datetime.toJavaInstant
import mu.KotlinLogging
import java.time.Instant
import kotlin.uuid.toJavaUuid

private val logger = KotlinLogging.logger { }

class R2dbcAssetRepository(
    private val connectionFactory: ConnectionFactory,
    private val library: Library
) : AssetRepository {
    private val selectColumns = """
            assets.id, 
            collection_id, 
            name,
            media_type, 
            content_hash,
            original_created_at, 
            original_filename, 
            created_at, 
            last_modified_at
    """.trimIndent()

    private val selectAssetsQuery = """
        select 
            $selectColumns
        from assets
        where collection_id = $1
        order by assets.created_at
    """.trimIndent()
    private val selectAssetByIdQuery = """
        select 
            $selectColumns
        from assets
        where assets.collection_id = $1 and assets.id = $2
    """.trimIndent()

    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? {
        return connectionFactory.useConnection {
            createStatement(selectAssetByIdQuery)
                .bind("$1", collectionId.value.toJavaUuid())
                .bind("$2", assetId.value.toJavaUuid())
                .execute()
                .awaitSingle()
                .mapToDomain()
                .firstOrNull()
        }
    }

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return connectionFactory.useConnection {
            createStatement(selectAssetsQuery)
                .bind("$1", collectionId.value.toJavaUuid())
                .execute()
                .awaitSingle()
                .mapToDomain()
                .toList()
        }
    }

    override suspend fun save(asset: Asset) {
        connectionFactory.useConnection {
            try {
                useTransaction {
                    when (asset) {
                        is FileAsset -> saveToAssetTable(asset)
                    }
                }
            } catch (throwable: Throwable) {
                throw SaveAssetException(throwable)
            }
        }
    }

    override suspend fun delete(id: AssetId) {
        connectionFactory.useConnection {
            try {
                useTransaction {
                    val rowsUpdated = arrayOf(
                        "DELETE FROM images WHERE id = $1",
                        "DELETE FROM videos WHERE id = $1",
                        "DELETE FROM assets WHERE id = $1",
                    ).map { query ->
                        createStatement(query)
                            .bind("$1", id.value.toJavaUuid())
                            .execute()
                            .awaitSingle()
                            .rowsUpdated
                            .awaitSingle()
                    }.sum()

                    if (rowsUpdated > 2) {
                        rollbackTransaction()
                        throw DeleteAssetException("More than one asset updated. Transaction rolled back.")
                    }
                }
            } catch (throwable: Throwable) {
                throw DeleteAssetException(throwable)
            }

        }
    }

    override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> {
        return library.readContentForAssetAsFlow(asset)
    }

    override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        require(asset.mediaTypeIsNotDefined) { "Cannot overwrite content for an asset" }

        val writeResult = library.writeContentForAsset(asset, content)

        asset.registerContentDetails(writeResult.mediaType, writeResult.contentHash)

        save(asset)
    }

    private suspend fun Connection.saveToAssetTable(asset: FileAsset) {
        val rowsUpdated = createStatement(
            """
                INSERT INTO assets
                (
                    id, 
                    collection_id, 
                    name,
                    original_created_at, 
                    original_filename,
                    created_at, 
                    last_modified_at
                )
                VALUES ($1, $2, $3, $4, $5, $6, $7)
                ON CONFLICT (id)
                DO UPDATE SET 
                    name = EXCLUDED.name,
                    media_type = $8,
                    content_hash = $9,
                    last_modified_at = EXCLUDED.last_modified_at
            """.trimIndent()
        )
            .bind("$1", asset.id.value.toJavaUuid())
            .bind("$2", asset.collectionId.value.toJavaUuid())
            .bind("$3", asset.name)
            .bind("$4", Instant.now()) // TODO: Remove here and add to some kind of metadata area
            .bind("$5", asset.originalFilename)
            .bind("$6", asset.auditFields.createdOn.toJavaInstant())
            .bind("$7", asset.auditFields.lastModifiedOn.toJavaInstant())
            .bindNullable("$8", asset.mediaType?.toString(), String::class.java)
            .bindNullable("$9", asset.contentHash, ByteArray::class.java)
            .execute()
            .awaitSingle()
            .rowsUpdated
            .awaitSingle()

        if (rowsUpdated > 1) {
            throw SaveAssetException("More than one row updated while saving in asset table.")
        }
    }

    private fun Result.mapToDomain(): Flow<Asset> {
        return map { row, _ ->
            row.toAsset()
        }.asFlow()
    }

    private fun Row.toAsset(): Asset {
        val mediaType = getString("media_type")?.let { MediaType.parse(it) }

        return FileAsset(
            id = AssetId(getUuid("id")),
            collectionId = CollectionId(getUuid("collection_id")),
            name = getString("name")!!,
            originalFilename = getString("original_filename")!!,
            mediaType = mediaType,
            contentHash = getByteArray("content_hash"),
            auditFields = toAuditFields()
        )
    }
}

private fun Row.toAuditFields(): AuditFields {
    return AuditFields(
        createdOn = getTimestamp("created_at")!!,
        lastModifiedOn = getTimestamp("last_modified_at")!!
    )
}

