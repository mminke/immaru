package com.earthrevealed.immaru.test.support

import com.earthrevealed.immaru.domain.Collection
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.Tag
import com.earthrevealed.immaru.domain.Video
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import com.earthrevealed.immaru.persistence.TagRepository

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

    fun save(image: Image) {
        assetRepository.save(image)
    }

    fun save(video: Video) {
        assetRepository.save(video)
    }
}