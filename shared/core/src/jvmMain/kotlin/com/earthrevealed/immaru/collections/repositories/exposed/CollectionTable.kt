package com.earthrevealed.immaru.collections.repositories.exposed

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

private const val SCHEMA_NAME = "immaru"

internal object CollectionTable : UUIDTable("$SCHEMA_NAME.collections") {
    val name = text("name")
//    val createdAt = timestamp("created_at")
}

internal fun InsertStatement<Number>.from(collection: Collection) {
    this[CollectionTable.id] = collection.id.toEntityId()
    this[CollectionTable.name] = collection.name
//    this[CollectionTable.createdAt] = collection.createdAt.toInstant()
}

internal fun ResultRow.toCollection() = Collection(
    id = CollectionId(this[CollectionTable.id].value),
    name = this[CollectionTable.name],
    createdAt = "not implemented yet"
//        createdAt = OffsetDateTime.ofInstant(this[CollectionTable.createdAt], ZoneId.systemDefault())
)

internal fun CollectionId.toEntityId() =
    EntityID(this.value, CollectionTable)
