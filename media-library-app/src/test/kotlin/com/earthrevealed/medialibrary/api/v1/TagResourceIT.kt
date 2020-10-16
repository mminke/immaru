package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.collection
import com.earthrevealed.medialibrary.persistence.AssetRepository
import com.earthrevealed.medialibrary.persistence.CollectionRepository
import com.earthrevealed.medialibrary.persistence.TagRepository
import com.earthrevealed.medialibrary.test.support.PersistenceMixin
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
internal class TagResourceIT: PersistenceMixin {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired override lateinit var assetRepository: AssetRepository
    @Autowired override lateinit var collectionRepository: CollectionRepository
    @Autowired override lateinit var tagRepository: TagRepository

    @Test
    fun `when retrieving tags for an unknown collection then not found is returned`() {
        val collectionId = UUID.randomUUID().toString()

        mockMvc.get("/collections/{collectionId}/tags/", collectionId) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    fun `given no tags exist when retrieving tags for an known collection then empty list is returned`() {
        val collection = collection { }.also { save(it) }

        mockMvc.get("/collections/{collectionId}/tags/", collection.id.value) {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content {
                json("[]")
            }
        }
    }

}