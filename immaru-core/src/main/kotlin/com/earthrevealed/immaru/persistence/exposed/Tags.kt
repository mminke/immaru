package com.earthrevealed.immaru.persistence.exposed

import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Tag
import com.earthrevealed.immaru.domain.TagId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime
import java.time.ZoneId

private const val SCHEMA_NAME = "immaru"

internal object TagTable : UUIDTable("${SCHEMA_NAME}.tags") {
    val collectionId = uuid("collection_id")
    val name = text("name")
    val createdAt = timestamp("created_at")
}

internal fun InsertStatement<Number>.from(tag: Tag) {
    this[TagTable.id] = EntityID(tag.id.value, AssetTable)
    this[TagTable.collectionId] = tag.collectionId.value
    this[TagTable.name] = tag.name
    this[TagTable.createdAt] = tag.createdAt.toInstant()
}

internal fun ResultRow.toTag() = Tag(
        id = TagId(this[TagTable.id].value),
        collectionId = CollectionId(this[TagTable.collectionId]),
        name = this[TagTable.name],
        createdAt = OffsetDateTime.ofInstant(this[TagTable.createdAt], ZoneId.systemDefault())
)

internal fun TagId.toEntityId() =
        EntityID(this.value, TagTable)