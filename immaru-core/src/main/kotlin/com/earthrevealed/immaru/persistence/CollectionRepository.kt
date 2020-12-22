package com.earthrevealed.immaru.persistence

import com.earthrevealed.immaru.domain.Collection
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.persistence.exposed.CollectionTable
import com.earthrevealed.immaru.persistence.exposed.from
import com.earthrevealed.immaru.persistence.exposed.toCollection
import com.earthrevealed.immaru.persistence.exposed.toEntityId
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class CollectionRepository {

    fun save(collection: Collection) {
        CollectionTable.insert { it.from(collection) }
    }

    fun all() =
            CollectionTable.selectAll()
                    .map { it.toCollection() }

    fun get(id: CollectionId) =
            CollectionTable.select { CollectionTable.id eq id.toEntityId() }
                    .firstOrNull()?.toCollection()

    fun delete(collection: Collection) {
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
