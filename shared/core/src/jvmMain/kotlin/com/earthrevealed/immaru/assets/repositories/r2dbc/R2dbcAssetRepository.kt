package com.earthrevealed.immaru.assets.repositories.r2dbc

import com.benasher44.uuid.Uuid
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.collections.CollectionId
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.datetime.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class R2dbcAssetRepository(
    private val connectionFactory: ConnectionFactory
) : AssetRepository {
    private val tableName = "assets"

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return connectionFactory.create().awaitSingle()
            .createStatement("SELECT * FROM $tableName ORDER BY created_at")
            .execute()
            .awaitSingle()
            .mapToDomain()
            .toList()
    }
}

private fun Result.mapToDomain(): Flow<Asset> {
    return map { row, _ ->
        val createdAtTimestamp =
            row.get("created_at", LocalDateTime::class.java)!!.atZone(ZoneId.systemDefault())
        val createdAt = Instant.fromEpochMilliseconds(createdAtTimestamp.toInstant().toEpochMilli())

        Asset(
            id = AssetId(row.get("id", Uuid::class.java)!!),
            collectionId = CollectionId(row.get("collection_id", Uuid::class.java)!!),
            originalFilename = row.get("original_filename", String::class.java)!!,
        )
    }.asFlow()
}