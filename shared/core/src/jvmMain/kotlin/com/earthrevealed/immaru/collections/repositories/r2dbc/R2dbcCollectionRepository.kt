package com.earthrevealed.immaru.collections.repositories.r2dbc

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import java.time.LocalDateTime
import java.util.UUID

class R2dbcCollectionRepository(
    private val connectionFactory: ConnectionFactory
) : CollectionRepository {
    private val tableName = "collections"

    override suspend fun save(collection: Collection) {
        TODO("Not yet implemented")
    }

    override suspend fun all(): List<Collection> {
        return connectionFactory.create().awaitSingle()
            .createStatement("SELECT * FROM $tableName ORDER BY name")
            .execute()
            .awaitSingle()
            .mapToDomain()
            .toList()
    }

    override suspend fun get(id: CollectionId): Collection? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(collection: Collection) {
        TODO("Not yet implemented")
    }
}

private fun Result.mapToDomain(): Flow<Collection> {
    return map { row, _ ->
        Collection(
            id = CollectionId(row.get("id", UUID::class.java)),
            name = row.get("name", String::class.java)!!,
            //TODO: Change to OffsetDateTime
            createdAt = row.get("created_at", LocalDateTime::class.java)!!.toString(),
        )
    }.asFlow()
}