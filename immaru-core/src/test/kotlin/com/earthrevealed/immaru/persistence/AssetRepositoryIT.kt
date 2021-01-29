package com.earthrevealed.immaru.persistence

import com.earthrevealed.immaru.domain.Height
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_JPEG
import com.earthrevealed.immaru.domain.OriginalDateOfCreation
import com.earthrevealed.immaru.domain.Width
import com.earthrevealed.immaru.domain.collection
import com.earthrevealed.immaru.domain.image
import com.earthrevealed.immaru.domain.px
import com.earthrevealed.immaru.domain.tag
import com.earthrevealed.immaru.test.support.PersistenceMixin
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
internal class AssetRepositoryIT : PersistenceMixin {

    @Autowired override lateinit var collectionRepository: CollectionRepository
    @Autowired override lateinit var tagRepository: TagRepository
    @Autowired override lateinit var assetRepository: AssetRepository

    @Test
    fun `test saving and retrieving an image`() {
        val collection = collection { }.also { save(it) }
        val asset = image(collection.id) { mediaType = MEDIATYPE_IMAGE_JPEG; originalFilename = "test-file.jpg" }
        assetRepository.save(asset)

        val retrievedAsset = assetRepository.get(collection.id, asset.id)
        retrievedAsset `should be equal to` asset
    }

    @Test
    fun `test saving and retrieving an image with tags`() {
        val collection = collection { }.also { save(it) }
        val tag1 = tag(collection.id) { name = "tag 1" }.also { save(it) }
        val tag2 = tag(collection.id) { name = "tag 2" }.also { save(it) }
        val asset = image(collection.id) {
            mediaType = MEDIATYPE_IMAGE_JPEG
            originalFilename = "test-file.jpg"
            tagIds = mutableSetOf(tag1.id, tag2.id)
        }
        assetRepository.save(asset)

        val result = assetRepository.get(collection.id, asset.id)
        result `should be equal to` asset
    }

    @Test
    fun `test retrieving multiple images`() {
        val collection = collection { }.also { save(it) }
        val asset1 = image(collection.id) { mediaType = MEDIATYPE_IMAGE_JPEG; originalFilename = "test-file.jpg" }.also { save(it) }
        val asset2 = image(collection.id) { mediaType = MEDIATYPE_IMAGE_JPEG; originalFilename = "test-file.jpg" }.also { save(it) }
        val asset3 = image(collection.id) { mediaType = MEDIATYPE_IMAGE_JPEG; originalFilename = "test-file.jpg" }.also { save(it) }

        val result = assetRepository.all(collection.id)

        result `should contain all` setOf(asset1, asset2, asset3)
    }

    @Test
    fun `test saving an image with specific image fields`() {
        val collection = collection {  }.also { save(it) }
        val image = image(collection.id) {
            mediaType = MEDIATYPE_IMAGE_JPEG
            originalFilename = "test-file.jpg"
            originalDateOfCreation = OriginalDateOfCreation.of(Instant.ofEpochSecond(1611611734))
            width = Width.of(3400.px)
            height = Height.of(2100.px)
        }

        assetRepository.save(image)

        val result = assetRepository.get(collection.id, image.id) as Image

        result `should be equal to` image
        result.width.value `should be equal to` 3400.px
        result.height.value `should be equal to` 2100.px
    }
}
