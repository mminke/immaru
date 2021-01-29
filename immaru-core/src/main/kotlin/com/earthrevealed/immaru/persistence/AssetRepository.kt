package com.earthrevealed.immaru.persistence

import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.persistence.exposed.AssetTable
import com.earthrevealed.immaru.persistence.exposed.AssetTagTable
import com.earthrevealed.immaru.persistence.exposed.ImageTable
import com.earthrevealed.immaru.persistence.exposed.from
import com.earthrevealed.immaru.persistence.exposed.toAsset
import com.earthrevealed.immaru.persistence.exposed.toEntityId
import com.earthrevealed.immaru.persistence.exposed.toTagId
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class AssetRepository {
    val AssetsJoinedWithImages = AssetTable.join(ImageTable, JoinType.LEFT, AssetTable.id, ImageTable.id)

    fun save(asset: Asset) {
        when(asset.mediaType.type) {
            MEDIATYPE_IMAGE.type -> save(asset as Image)
            MEDIATYPE_VIDEO.type -> TODO()
        }
    }

    fun save(image: Image) {
        AssetTable.insert { it.from(image as Asset) }
        ImageTable.insert { it.from(image) }

        updateTagsFor(image)
    }

    fun updateTagsFor(asset: Asset) {
        AssetTagTable.deleteWhere { AssetTagTable.assetId eq asset.id.value }

        asset.tagIds.forEach { tagId ->
            AssetTagTable
                    .insert {
                        it[this.assetId] = asset.id.value
                        it[this.tagId] = tagId.value
                    }
        }
    }

    fun all(collectionId: CollectionId) =
            AssetsJoinedWithImages
                    .select {
                        AssetTable.collectionId eq collectionId.value
                    }.orderBy(AssetTable.originalCreatedAt, SortOrder.DESC)
                    .map { assetRecord ->
                        assetRecord.toAsset {
                            tagIdsForAsset(assetRecord[AssetTable.id].value)
                        }
                    }

    fun get(collectionId: CollectionId, id: AssetId): Asset? =
            AssetsJoinedWithImages
                    .select { AssetTable.id eq id.toEntityId() }
                    .andWhere { AssetTable.collectionId eq collectionId.value }
                    .firstOrNull()?.let { assetRecord ->
                        assetRecord.toAsset {
                            tagIdsForAsset(assetRecord[AssetTable.id].value)
                        }
                    }

    fun hasAssets(collectionId: CollectionId): Boolean =
            AssetTable
                    .select { AssetTable.collectionId eq collectionId.value }
                    .limit(1)
                    .toSet()
                    .isNotEmpty()

    private fun tagIdsForAsset(assetId: UUID): List<TagId> = AssetTagTable
            .select { AssetTagTable.assetId eq assetId }
            .map { assetTagRecord -> assetTagRecord.toTagId() }

}