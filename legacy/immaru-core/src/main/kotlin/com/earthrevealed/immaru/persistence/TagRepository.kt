package com.earthrevealed.immaru.persistence

import com.earthrevealed.immaru.assets.exposed.AssetTagTable
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Tag
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.persistence.exposed.TagTable
import com.earthrevealed.immaru.persistence.exposed.from
import com.earthrevealed.immaru.persistence.exposed.toEntityId
import com.earthrevealed.immaru.persistence.exposed.toTag
import org.jetbrains.exposed.sql.JoinType
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