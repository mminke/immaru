package com.earthrevealed.immaru.persistence.exposed

import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.CreatedAt
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.ImageHeight
import com.earthrevealed.immaru.domain.ImageWidth
import com.earthrevealed.immaru.domain.LastModifiedAt
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.OriginalDateOfCreation
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.domain.image
import com.earthrevealed.immaru.domain.px
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import javax.ws.rs.core.MediaType

internal object AssetTable : UUIDTable("assets") {
    val collectionId = uuid("collection_id")
    val mediaType = text("media_type")
    val originalFilename = text("original_filename")
    val originalCreatedAt = timestamp("original_created_at").nullable()
    val createdAt = timestamp("created_at")
    val lastModifiedAt = timestamp("last_modified_at")
}

internal object ImageTable : UUIDTable("images") {
    val imageWidth = integer( "image_width")
    val imageHeight = integer( "image_height")
}

internal object AssetTagTable : Table("asset_tags") {
    val assetId = uuid("asset_id").references(AssetTable.id)
    val tagId = uuid("tag_id").references(TagTable.id)
}

internal fun InsertStatement<Number>.from(asset: Asset) {
    this[AssetTable.id] = EntityID(asset.id.value, AssetTable)
    this[AssetTable.collectionId] = asset.collectionId.value
    this[AssetTable.mediaType] = asset.mediaType.toString()
    this[AssetTable.originalFilename] = asset.originalFilename
    this[AssetTable.originalCreatedAt] = asset.originalCreatedAt?.value?.toInstant()
    this[AssetTable.createdAt] = asset.audit.createdAt.value.toInstant()
    this[AssetTable.lastModifiedAt] = asset.audit.lastModifiedAt.value.toInstant()
}

internal fun InsertStatement<Number>.from(image: Image) {
    this[ImageTable.id] = EntityID(image.id.value, ImageTable)
    this[ImageTable.imageWidth] = image.imageWidth.value.value
    this[ImageTable.imageHeight] = image.imageHeight.value.value
}

internal fun ResultRow.toAsset(tags: () -> List<TagId>): Asset {
    val mediaType = MediaType.valueOf(this[AssetTable.mediaType])
    return when(mediaType.type) {
        MEDIATYPE_IMAGE.type -> image(CollectionId(this[AssetTable.collectionId])) {
            this.id = AssetId(this@toAsset[AssetTable.id].value)
            this.mediaType = mediaType
            this.originalFilename = this@toAsset[AssetTable.originalFilename]
            this.originalDateOfCreation = this@toAsset[AssetTable.originalCreatedAt]?.let { OriginalDateOfCreation.of(it) }

            this.imageWidth = ImageWidth.of(this@toAsset[ImageTable.imageWidth].px)
            this.imageHeight = ImageHeight.of(this@toAsset[ImageTable.imageHeight].px)

            this.tagIds = tags().toMutableSet()

            audit {
                createdAt = CreatedAt.of(this@toAsset[AssetTable.createdAt])
                lastModifiedAt = LastModifiedAt.of(this@toAsset[AssetTable.lastModifiedAt])
            }
        }
        MEDIATYPE_VIDEO.type -> TODO()
        else -> throw IllegalStateException("Media type from database is not known: ${mediaType}")
    }
}

internal fun ResultRow.toTagId() = TagId(this[AssetTagTable.tagId])

internal fun AssetId.toEntityId() =
        EntityID(this.value, AssetTable)