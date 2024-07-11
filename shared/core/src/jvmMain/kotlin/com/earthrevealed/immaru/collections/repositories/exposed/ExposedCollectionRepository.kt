package com.earthrevealed.immaru.collections.repositories.exposed

import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class ExposedCollectionRepository : CollectionRepository {

    override suspend fun save(collection: Collection) {
        CollectionTable.insert { it.from(collection) }
    }

    override suspend fun all() =
        CollectionTable.selectAll()
            .map { it.toCollection() }

    override suspend fun get(id: CollectionId) =
        CollectionTable.select { CollectionTable.id eq id.toEntityId() }
            .firstOrNull()?.toCollection()

    override suspend fun delete(collection: Collection) {
        CollectionTable.deleteWhere {
            CollectionTable.id eq collection.id.value
        }
    }

    fun notExists(collectionId: CollectionId): Boolean =
        CollectionTable
            .select { CollectionTable.id eq collectionId.value }
            .toSet()
            .isEmpty()
}
