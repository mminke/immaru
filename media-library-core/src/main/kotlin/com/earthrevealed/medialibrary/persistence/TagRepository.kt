package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.Tag
import com.earthrevealed.medialibrary.domain.TagId
import com.earthrevealed.medialibrary.persistence.exposed.TagTable
import com.earthrevealed.medialibrary.persistence.exposed.from
import com.earthrevealed.medialibrary.persistence.exposed.toEntityId
import com.earthrevealed.medialibrary.persistence.exposed.toTag
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class TagRepository {
    fun save(tag: Tag) {
        TagTable.insert { it.from(tag) }
    }

    fun all() =
        TagTable.selectAll()
                .map { it.toTag() }

    fun get(id: TagId) =
            TagTable.select { TagTable.id eq id.toEntityId() }
                    .firstOrNull()?.toTag()
}