package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.domain.Tag
import com.earthrevealed.medialibrary.domain.TagId
import com.earthrevealed.medialibrary.persistence.exposed.TagTable
import com.earthrevealed.medialibrary.persistence.exposed.from
import com.earthrevealed.medialibrary.persistence.exposed.toEntityId
import com.earthrevealed.medialibrary.persistence.exposed.toTag
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class TagRepository {
    fun save(tag: Tag) {
        TagTable.insert { it.from(tag) }
    }

    fun all(collectionId: CollectionId) =
        TagTable.select { TagTable.collectionId eq collectionId.value }
                .map { it.toTag() }

    fun get(collectionId: CollectionId, id: TagId) =
            TagTable.select { TagTable.id eq id.toEntityId() }
                    .andWhere { TagTable.collectionId eq collectionId.value }
                    .firstOrNull()?.toTag()
}