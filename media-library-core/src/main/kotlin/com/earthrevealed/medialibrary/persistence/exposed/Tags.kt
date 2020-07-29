package com.earthrevealed.medialibrary.persistence.exposed

import com.earthrevealed.medialibrary.domain.Tag
import com.earthrevealed.medialibrary.domain.TagId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime
import java.time.ZoneId

internal object TagTable : UUIDTable("tags") {
    val name = text("name")
    val createdAt = timestamp("created_at")
}

internal fun InsertStatement<Number>.from(tag: Tag) {
    this[TagTable.id] = EntityID(tag.id.value, AssetTable)
    this[TagTable.name] = tag.name
    this[TagTable.createdAt] = tag.createdAt.toInstant()
}

internal fun ResultRow.toTag() = Tag(
        id = TagId(this[TagTable.id].value),
        name = this[TagTable.name],
        createdAt = OffsetDateTime.ofInstant(this[TagTable.createdAt], ZoneId.systemDefault())
)

internal fun TagId.toEntityId() =
        EntityID(this.value, TagTable)