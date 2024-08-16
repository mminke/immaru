package com.earthrevealed.immaru.collections.repositories.r2dbc

import com.benasher44.uuid.Uuid
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.CollectionUpdateException
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.LocalDateTime
import java.time.ZoneId

class R2dbcCollectionRepository(
    private val connectionFactory: ConnectionFactory
) : CollectionRepository {
    private val tableName = "collections"

    override suspend fun save(collection: Collection) {
        val connection = connectionFactory.create().awaitSingle()
        connection.beginTransaction()

        try {
            val rowsUpdated =
                connection
                    .createStatement(
                        """
                        INSERT INTO $tableName
                        VALUES ($1, $2, $3)
                        ON CONFLICT (id)
                        DO UPDATE SET name = EXCLUDED.name
                        """.trimIndent()
                    )
                    .bind("$1", collection.id.value)
                    .bind("$2", collection.name)
                    .bind("$3", collection.createdAt.toJavaInstant())
                    .execute()
                    .awaitSingle()
                    .rowsUpdated
                    .awaitSingle()

            if (rowsUpdated > 1) {
                connection.rollbackTransaction()
                throw CollectionUpdateException("More than one row updated. Transaction rolled back.")
            }
            connection.commitTransaction()
        } catch (throwable: Throwable) {
            connection.rollbackTransaction()
            throw CollectionUpdateException(throwable)
        }
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

    override suspend fun delete(id: CollectionId) {
        val connection = connectionFactory.create().awaitSingle()
        connection.beginTransaction()

        try {
            val rowsUpdated =
                connection
                    .createStatement("DELETE FROM $tableName WHERE id = $1")
                    .bind("$1", id.value)
                    .execute()
                    .awaitSingle()
                    .rowsUpdated
                    .awaitSingle()

            if (rowsUpdated > 1) {
                connection.rollbackTransaction()
                throw CollectionUpdateException("More than one row updated. Transaction rolled back.")
            }
            connection.commitTransaction()
        } catch (throwable: Throwable) {
            connection.rollbackTransaction()
            throw CollectionUpdateException(throwable)
        }
    }
}

private fun Result.mapToDomain(): Flow<Collection> {
    return map { row, _ ->
        val createdAtTimestamp =
            row.get("created_at", LocalDateTime::class.java)!!.atZone(ZoneId.systemDefault())
        val createdAt = Instant.fromEpochMilliseconds(createdAtTimestamp.toInstant().toEpochMilli())

        Collection(
            id = CollectionId(row.get("id", Uuid::class.java)!!),
            name = row.get("name", String::class.java)!!,
            createdAt = createdAt,
        )
    }.asFlow()
}