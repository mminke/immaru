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
import kotlinx.io.Sink
import kotlinx.io.Source

class R2dbcAssetRepository(
    private val connectionFactory: ConnectionFactory,
    private val library: Library
) : AssetRepository {
    private val selectColumns = """
            assets.id, 
            collection_id, 
            name,
            media_type, 
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
                .bind("$1", collectionId.value)
                .bind("$2", assetId.value)
                .execute()
                .awaitSingle()
                .mapToDomain()
                .firstOrNull()
        }
    }

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return connectionFactory.useConnection {
            createStatement(selectAssetsQuery)
                .bind("$1", collectionId.value)
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
                            .bind("$1", id.value)
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

    override fun getContentFor(asset: FileAsset): Sink {
        TODO("Not yet implemented")
    }

    override suspend fun saveContentFor(asset: FileAsset, contentSource: Source) {
        require(asset.mediaTypeIsNotDefined) { "Cannot overwrite content for an asset"}

        val detectedMediaType =
            library.writeContentForAsset(asset, contentSource)
        asset.update {
            mediaType = detectedMediaType
        }

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
                    last_modified_at = EXCLUDED.last_modified_at,
                    media_type = $8
            """.trimIndent()
        )
            .bind("$1", asset.id.value)
            .bind("$2", asset.collectionId.value)
            .bind("$3", asset.name)
            .bind("$4", asset.originalCreatedOn.toJavaInstant())
            .bind("$5", asset.originalFilename)
            .bind("$6", asset.auditFields.createdOn.toJavaInstant())
            .bind("$7", asset.auditFields.lastModifiedOn.toJavaInstant())
            .bindNullable("$8", asset.mediaType?.toString(), String::class.java)
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
        val mediaType = get("media_type", String::class.java)?.let { MediaType.parse(it) }

        return FileAsset(
            id = AssetId(getUuid("id")),
            collectionId = CollectionId(getUuid("collection_id")),
            name = getString("name"),
            mediaType = mediaType,
            originalFilename = getString("original_filename"),
            originalCreatedOn = getTimestamp("original_created_at"),
            auditFields = toAuditFields()
        )
    }
}

private fun Row.toAuditFields(): AuditFields {
    return AuditFields(
        createdOn = getTimestamp("created_at"),
        lastModifiedOn = getTimestamp("last_modified_at")
    )
}

