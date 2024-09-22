package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.support.FixedClock
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.HOUR
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FileAssetTest {

    @BeforeTest
    fun setup() {
        ClockProvider.clock = FixedClock
        FixedClock.reset()
    }

    @Test
    fun `test creating new file asset`() {
        val collectionId = CollectionId()
        val originalCreatedOn = Instant.parse("2024-09-21T21:02:59.138453254Z")
        val fileAsset = FileAsset(
            collectionId = collectionId,
            originalFilename = "1234.jpg",
            originalCreatedOn = originalCreatedOn
        )

        assertNotNull(fileAsset.id)
        assertEquals(collectionId, fileAsset.collectionId)
        assertEquals("1234.jpg", fileAsset.name)
        assertEquals("1234.jpg", fileAsset.originalFilename)
        assertEquals(originalCreatedOn, fileAsset.originalCreatedOn)
        assertNull(fileAsset.mediaType)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.createdOn)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.lastModifiedOn)
    }

    @Test
    fun `test updating a file asset`() {
        val collectionId = CollectionId()
        val originalCreatedOn = Instant.parse("2024-09-21T21:02:59.138453254Z")
        val fileAsset = FileAsset(
            collectionId = collectionId,
            originalFilename = "1234.jpg",
            originalCreatedOn = originalCreatedOn
        )

        FixedClock.setClockTo(FixedClock.DEFAULT_INSTANT.plus(1, HOUR))

        fileAsset.update {
            name = "a new name"
            mediaType = MediaType.IMAGE_JPEG
        }

        assertNotNull(fileAsset.id)
        assertEquals(collectionId, fileAsset.collectionId)
        assertEquals("a new name", fileAsset.name)
        assertEquals("1234.jpg", fileAsset.originalFilename)
        assertEquals(originalCreatedOn, fileAsset.originalCreatedOn)
        assertEquals(MediaType.IMAGE_JPEG, fileAsset.mediaType)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.createdOn)
        assertEquals(FixedClock.DEFAULT_INSTANT.plus(1, HOUR), fileAsset.auditFields.lastModifiedOn)
    }

    @Test
    fun testFileAssetSerialization() {
        val fileAsset = FileAsset(
            collectionId = CollectionId(),
            originalFilename = "1234.jpg",
            originalCreatedOn = Clock.System.now()
        )

        val json = Json { ignoreUnknownKeys = true }
        println()
        println("asset.name: ${fileAsset.name}")
        println(json.encodeToString(Asset.serializer(), fileAsset))

        fileAsset.update {
            name = "some name"
            mediaType = MediaType.IMAGE_JPEG
        }
        val jsonText = json.encodeToString(Asset.serializer(), fileAsset)
        println()
        println()
        println(jsonText)

        val asset = Json.decodeFromString<Asset>(jsonText)
        assertEquals(fileAsset.id, asset.id)
        assertEquals("some name", asset.name)
        assertNotNull(asset.auditFields.lastModifiedOn)
        if (asset is FileAsset) {
            assertEquals("1234.jpg", asset.originalFilename)
            assertEquals(fileAsset.originalCreatedOn, asset.originalCreatedOn)
            assertEquals(MediaType.IMAGE_JPEG, asset.mediaType)
        }
    }
}