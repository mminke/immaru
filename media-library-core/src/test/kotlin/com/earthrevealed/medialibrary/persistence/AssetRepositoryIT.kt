package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.asset
import com.earthrevealed.medialibrary.domain.collection
import com.earthrevealed.medialibrary.domain.tag
import com.earthrevealed.medialibrary.test.support.PersistenceMixin
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class AssetRepositoryIT : PersistenceMixin {

    @Autowired override lateinit var collectionRepository: CollectionRepository
    @Autowired override lateinit var tagRepository: TagRepository
    @Autowired override lateinit var assetRepository: AssetRepository

    @Test
    fun `test saving and retrieving an asset`() {
        val collection = collection { }.also { save(it) }
        val asset = asset(collection.id) { originalFilename = "test-file.jpg" }
        assetRepository.save(asset)

        val result = assetRepository.get(collection.id, asset.id)
        result `should be equal to` asset
    }

    @Test
    fun `test saving and retrieving an asset with tags`() {
        val collection = collection { }.also { save(it) }
        val tag1 = tag(collection.id) { name = "tag 1" }.also { save(it) }
        val tag2 = tag(collection.id) { name = "tag 2" }.also { save(it) }
        val asset = asset(collection.id) {
            originalFilename = "test-file.jpg"
            tagIds = mutableSetOf(tag1.id, tag2.id)
        }
        assetRepository.save(asset)

        val result = assetRepository.get(collection.id, asset.id)
        result `should be equal to` asset
    }

    @Test
    fun `retrieving multiple assets`() {
        val collection = collection { }.also { save(it) }
        val asset1 = asset(collection.id) { originalFilename = "test-file.jpg" }.also { save(it) }
        val asset2 = asset(collection.id) { originalFilename = "test-file.jpg" }.also { save(it) }
        val asset3 = asset(collection.id) { originalFilename = "test-file.jpg" }.also { save(it) }

        val result = assetRepository.all(collection.id)

        result `should contain all` setOf(asset1, asset2, asset3)
    }
}