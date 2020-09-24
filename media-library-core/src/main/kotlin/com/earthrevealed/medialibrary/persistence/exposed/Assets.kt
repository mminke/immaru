package com.earthrevealed.medialibrary.persistence.exposed

import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime
import java.time.ZoneId

internal object AssetTable : UUIDTable("assets") {
    val collectionId = uuid("collection_id")
    val originalFilename = text("original_filename")
    val createdAt = timestamp("created_at")
}

internal fun InsertStatement<Number>.from(asset: Asset) {
    this[AssetTable.id] = EntityID(asset.id.value, AssetTable)
    this[AssetTable.originalFilename] = asset.originalFilename
    this[AssetTable.createdAt] = asset.createdAt.toInstant()
}

internal fun ResultRow.toAsset() = Asset(
        id = AssetId(this[AssetTable.id].value),
        collectionId = CollectionId(this[AssetTable.collectionId]),
        originalFilename = this[AssetTable.originalFilename],
        createdAt = OffsetDateTime.ofInstant(this[AssetTable.createdAt], ZoneId.systemDefault())
)

internal fun AssetId.toEntityId() =
        EntityID(this.value, AssetTable)