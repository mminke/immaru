package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.assets.MediaType.Companion.IMAGE_JPEG
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.support.FixedClock
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.HOUR
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
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

    @AfterTest
    fun tearDown() {
        ClockProvider.clock = Clock.System
    }

    @Test
    fun `test creating new file asset`() {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(
            collectionId = collectionId,
            originalFilename = "1234.jpg",
        )

        assertNotNull(fileAsset.id)
        assertEquals(collectionId, fileAsset.collectionId)
        assertEquals("1234.jpg", fileAsset.name)
        assertEquals("1234.jpg", fileAsset.originalFilename)
        assertNull(fileAsset.mediaType)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.createdOn)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.lastModifiedOn)
    }

    @Test
    fun `test updating a file asset`() {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(
            collectionId = collectionId,
            originalFilename = "1234.jpg",
        )

        FixedClock.setClockTo(FixedClock.DEFAULT_INSTANT.plus(1, HOUR))

        fileAsset.changeName("a new name")
        fileAsset.registerContentDetails(IMAGE_JPEG, ByteArray(0))

        assertNotNull(fileAsset.id)
        assertEquals(collectionId, fileAsset.collectionId)
        assertEquals("a new name", fileAsset.name)
        assertEquals("1234.jpg", fileAsset.originalFilename)
        assertEquals(IMAGE_JPEG, fileAsset.mediaType)
        assertEquals(FixedClock.DEFAULT_INSTANT, fileAsset.auditFields.createdOn)
        assertEquals(FixedClock.DEFAULT_INSTANT.plus(1, HOUR), fileAsset.auditFields.lastModifiedOn)
    }

    @Test
    fun testFileAssetSerialization() {
        val fileAsset = FileAsset(
            collectionId = CollectionId(),
            originalFilename = "1234.jpg",
        )

        val json = Json { ignoreUnknownKeys = true }

        fileAsset.changeName("some name")
        fileAsset.registerContentDetails(IMAGE_JPEG, ByteArray(0))

        val jsonText = json.encodeToString(Asset.serializer(), fileAsset)

        val asset = Json.decodeFromString<Asset>(jsonText)
        assertEquals(fileAsset.id, asset.id)
        assertEquals("some name", asset.name)
        assertNotNull(asset.auditFields.lastModifiedOn)
        if (asset is FileAsset) {
            assertEquals("1234.jpg", asset.originalFilename)
            assertEquals(IMAGE_JPEG, asset.mediaType)
        }
    }
}