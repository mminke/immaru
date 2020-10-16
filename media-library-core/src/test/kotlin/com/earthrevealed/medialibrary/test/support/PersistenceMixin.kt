package com.earthrevealed.medialibrary.test.support

import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.Collection
import com.earthrevealed.medialibrary.domain.Tag
import com.earthrevealed.medialibrary.persistence.AssetRepository
import com.earthrevealed.medialibrary.persistence.CollectionRepository
import com.earthrevealed.medialibrary.persistence.TagRepository

interface PersistenceMixin {
    var collectionRepository: CollectionRepository
    var tagRepository: TagRepository
    var assetRepository: AssetRepository

    fun save(collection: Collection) {
        collectionRepository.save(collection)
    }

    fun save(tag: Tag) {
        tagRepository.save(tag)
    }

    fun save(asset: Asset) {
        assetRepository.save(asset)
    }
}