package com.earthrevealed.immaru.assets.exposed

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.Image
import com.earthrevealed.immaru.assets.Video
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.TagId
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
class ExposedAssetRepository: AssetRepository {
    val AssetsJoinedWithImagesAndVideos = AssetTable
            .join(ImageTable, JoinType.LEFT, AssetTable.id, ImageTable.id)
            .join(VideoTable, JoinType.LEFT, AssetTable.id, VideoTable.id)

    val AssetsJoinedWithImagesVideosAndAssetTags = AssetsJoinedWithImagesAndVideos
            .join(AssetTagTable, JoinType.LEFT, AssetTable.id, AssetTagTable.assetId)

    override fun save(image: Image) {
        AssetTable.insert { it.from(image as Asset) }
        ImageTable.insert { it.from(image) }

        updateTagsFor(image)
    }

    override fun save(video: Video) {
        AssetTable.insert { it.from(video as Asset) }
        VideoTable.insert { it.from(video) }

        updateTagsFor(video)
    }

    override fun updateTagsFor(asset: Asset) {
        AssetTagTable.deleteWhere { AssetTagTable.assetId eq asset.id.value }

        asset.tagIds.forEach { tagId ->
            AssetTagTable
                    .insert {
                        it[this.assetId] = asset.id.value
                        it[this.tagId] = tagId.value
                    }
        }
    }

    override fun all(collectionId: CollectionId) =
            AssetsJoinedWithImagesAndVideos
                    .select {
                        AssetTable.collectionId eq collectionId.value
                    }.orderBy(AssetTable.originalCreatedAt, SortOrder.DESC)
                    .map { assetRecord ->
                        assetRecord.toAsset {
                            tagIdsForAsset(assetRecord[AssetTable.id].value)
                        }
                    }

    override fun findByTags(collectionId: CollectionId, tagIds: Set<TagId>?): List<Asset> {
        val query = AssetsJoinedWithImagesVideosAndAssetTags.select {
            AssetTable.collectionId eq collectionId.value
        }
        tagIds?.let {
            query.andWhere {
                AssetTagTable.tagId inList (it.map { tagId -> tagId.value }.asIterable())
            }
        }

        return query
                .orderBy(AssetTable.originalCreatedAt, SortOrder.DESC)
                .orderBy(AssetTable.createdAt, SortOrder.DESC)
                .orderBy(AssetTable.originalFilename, SortOrder.DESC)
                .map { resultRow ->
                    resultRow.toAsset {
                        tagIdsForAsset(resultRow[AssetTable.id].value)
                    }
                }
    }

    override fun get(collectionId: CollectionId, id: AssetId): Asset? =
            AssetsJoinedWithImagesAndVideos
                    .select { AssetTable.id eq id.toEntityId() }
                    .andWhere { AssetTable.collectionId eq collectionId.value }
                    .firstOrNull()?.let { assetRecord ->
                        assetRecord.toAsset {
                            tagIdsForAsset(assetRecord[AssetTable.id].value)
                        }
                    }

    override fun hasAssets(collectionId: CollectionId): Boolean =
            AssetTable
                    .select { AssetTable.collectionId eq collectionId.value }
                    .limit(1)
                    .toSet()
                    .isNotEmpty()

    private fun tagIdsForAsset(assetId: UUID): List<TagId> = AssetTagTable
            .select { AssetTagTable.assetId eq assetId }
            .map { assetTagRecord -> assetTagRecord.toTagId() }

}