package com.earthrevealed.immaru.persistence.exposed

import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.MediaType
import com.earthrevealed.immaru.domain.TagId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime
import java.time.ZoneId

internal object AssetTable : UUIDTable("assets") {
    val collectionId = uuid("collection_id")
    val originalFilename = text("original_filename")
    val mediaType = text("media_type")
    val createdAt = timestamp("created_at")
}

internal object AssetTagTable : Table("asset_tags") {
    val assetId = uuid("asset_id").references(AssetTable.id)
    val tagId = uuid("tag_id").references(TagTable.id)
}

internal fun InsertStatement<Number>.from(asset: Asset) {
    this[AssetTable.id] = EntityID(asset.id.value, AssetTable)
    this[AssetTable.collectionId] = asset.collectionId.value
    this[AssetTable.originalFilename] = asset.originalFilename
    this[AssetTable.mediaType] = asset.mediaType.value
    this[AssetTable.createdAt] = asset.createdAt.toInstant()
}

internal fun ResultRow.toAsset(tags: () -> List<TagId>) = Asset(
        id = AssetId(this[AssetTable.id].value),
        collectionId = CollectionId(this[AssetTable.collectionId]),
        mediaType = MediaType(this[AssetTable.mediaType]),
        originalFilename = this[AssetTable.originalFilename],
        createdAt = OffsetDateTime.ofInstant(this[AssetTable.createdAt], ZoneId.systemDefault()),
        tagIds = tags().toMutableSet()
)

internal fun ResultRow.toTagId() = TagId(this[AssetTagTable.tagId])

internal fun AssetId.toEntityId() =
        EntityID(this.value, AssetTable)