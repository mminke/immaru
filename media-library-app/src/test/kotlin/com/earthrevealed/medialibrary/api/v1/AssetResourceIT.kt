package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.asset
import com.earthrevealed.medialibrary.domain.collection
import com.earthrevealed.medialibrary.domain.tag
import com.earthrevealed.medialibrary.persistence.AssetRepository
import com.earthrevealed.medialibrary.persistence.CollectionRepository
import com.earthrevealed.medialibrary.persistence.TagRepository
import com.earthrevealed.medialibrary.test.support.PersistenceMixin
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
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
            status { isNotFound }
        }
    }

    @Test
    fun `given no assets exist when retrieving assets for an known collection then empty list is returned`() {
        val collection = collection { }.also { save(it) }

        mockMvc.get("/collections/{collectionId}/assets/", collection.id.value) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content {
                json("[]")
            }
        }
    }

    @Test
    fun `given an assets exist when retrieving assets for an known collection then the asset is returned`() {
        val collection = collection { }.also { save(it) }
        val tag = tag(collection.id) { name = "tag" }.also { save(it) }
        val asset = asset(collection.id) {
            originalFilename = "test.jpg"
            tagIds = mutableSetOf(tag.id)
        }.also { save(it) }
        val expected = objectMapper.writeValueAsString(asset)

        mockMvc.get("/collections/{collectionId}/assets/", collection.id.value) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content {
                json("[$expected]")
            }
        }
    }
}