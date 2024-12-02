package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.TagId

interface AssetRepository {
    fun save(image: Image)
    fun save(video: Video)
    fun updateTagsFor(asset: Asset)
    fun all(collectionId: CollectionId): List<Asset>
    fun findByTags(collectionId: CollectionId, tagIds: Set<TagId>?): List<Asset>
    fun get(collectionId: CollectionId, id: AssetId): Asset?
    fun hasAssets(collectionId: CollectionId): Boolean
}