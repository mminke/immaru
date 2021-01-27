package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_JPEG
import com.earthrevealed.immaru.domain.collection
import com.earthrevealed.immaru.domain.image
import com.earthrevealed.immaru.domain.tag
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import com.earthrevealed.immaru.persistence.TagRepository
import com.earthrevealed.immaru.test.support.PersistenceMixin
import com.fasterxml.jackson.databind.ObjectMapper
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.util.*


@SpringBootTest
@AutoConfigureMockMvc
internal class AssetResourceIT : PersistenceMixin {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired override lateinit var assetRepository: AssetRepository
    @Autowired override lateinit var collectionRepository: CollectionRepository
    @Autowired override lateinit var tagRepository: TagRepository

    @Test
    fun `when retrieving assets for an unknown collection then not found is returned`() {
        val collectionId = UUID.randomUUID().toString()

        mockMvc.get("/collections/{collectionId}/assets/", collectionId) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `given no assets exist when retrieving assets for an known collection then empty list is returned`() {
        val collection = collection { }.also { save(it) }

        mockMvc.get("/collections/{collectionId}/assets/", collection.id.value) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                json("[]")
            }
        }
    }

    @Test
    fun `given an assets exist when retrieving assets for an known collection then the asset is returned`() {
        val collection = collection { }.also { save(it) }
        val tag = tag(collection.id) { name = "tag" }.also { save(it) }
        val image = image(collection.id) {
            mediaType = MEDIATYPE_IMAGE_JPEG
            originalFilename = "test.jpg"
            tagIds = mutableSetOf(tag.id)
        }.also { save(it) }
        val expected = objectMapper.writeValueAsString(image)

        mockMvc.get("/collections/{collectionId}/assets/", collection.id.value) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                json("[$expected]")
            }
        }
    }

    @Test
    fun `given an assets when updating the tags then the changes are correctly persisted`() {
        val collection = collection { }.also { save(it) }
        val tag = tag(collection.id) { name = "tag 1" }.also { save(it) }
        val tag2 = tag(collection.id) { name = "tag 2"}.also { save(it) }
        val tag3 = tag(collection.id) { name = "tag 3"}.also { save(it) }
        val asset = image(collection.id) {
            mediaType = MEDIATYPE_IMAGE_JPEG
            originalFilename = "test.jpg"
            tagIds = setOf(tag.id)
        }.also { save(it) }

        mockMvc.put("/collections/{collectionId}/assets/{assetId}/tags", collection.id.value, asset.id.value) {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = "[\"${tag2.id.value.toString()}\", \"${tag3.id.value.toString()}\"]"
        }.andExpect {
            status { isAccepted() }
        }

        val savedAsset = assetRepository.get(collection.id, asset.id)!!

        savedAsset.tagIds `should contain same` setOf(tag2.id, tag3.id)
    }
}