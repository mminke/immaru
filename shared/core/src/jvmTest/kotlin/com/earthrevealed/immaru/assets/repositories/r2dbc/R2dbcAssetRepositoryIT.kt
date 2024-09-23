package com.earthrevealed.immaru.assets.repositories.r2dbc

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import com.earthrevealed.immaru.common.AuditFields
import com.earthrevealed.immaru.support.truncateNanos
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class R2dbcAssetRepositoryIT {
    private var connectionFactory: ConnectionFactory =
        ConnectionFactories.get("r2dbc:postgresql://immaru_dev:immaru_dev@localhost:5432/immaru_dev?schema=immaru")
    private var collectionRepository = R2dbcCollectionRepository(connectionFactory)
    private var library = Library(Files.createTempDirectory("immaru-testing"))
    private var assetRepository = R2dbcAssetRepository(connectionFactory, library)
    private val collection = collection { name = "Test collection" }

    @BeforeTest
    fun setup() {
        runBlocking {
            collectionRepository.save(collection)
        }
    }

    @AfterTest
    fun tearDown() {
//        runBlocking {
//            assetRepository.findAllFor(collection.id).forEach { asset ->
//                assetRepository.delete(asset.id)
//            }
//            collectionRepository.delete(collection.id)
//        }
    }

    @Test
    fun `test saving a new file asset and retrieving it`() {
        val fileAsset = FileAsset(
            AssetId(),
            collection.id,
            "test.jpg",
            mediaType = MediaType.IMAGE_JPEG,
            "1234.jpg",
            Clock.System.now(),
            AuditFields(),
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val results = assetRepository.findAllFor(collection.id)
            assertEquals(1, results.size)
            assertEquals(fileAsset, results[0])

            val result = assetRepository.findById(collection.id, fileAsset.id)
            assertNotNull(result)
            assertEquals(fileAsset, result)
        }
    }

    @Test
    fun `test saving a new file asset without media-type and retrieving it`() {
        val fileAsset = FileAsset(
            collectionId = collection.id,
            originalFilename = "1234.jpg",
            originalCreatedOn = Clock.System.now(),
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val result = assetRepository.findById(collection.id, fileAsset.id)
            assertNotNull(result)
            assertEquals(fileAsset, result)
        }
    }

    @Test
    fun `test saving a new file asset and updating it`() {
        val fileAsset = FileAsset(
            collectionId = collection.id,
            originalFilename = "1234.jpg",
            originalCreatedOn = Clock.System.now(),
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val assetToModify = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset
            assertEquals("1234.jpg", assetToModify.name)
            assertNull(assetToModify.mediaType)
            assertEquals(
                fileAsset.auditFields.lastModifiedOn.truncateNanos(),
                assetToModify.auditFields.createdOn.truncateNanos()
            )

            delay(1000)

            assetToModify.update {
                name = "my new name"
                mediaType = MediaType.IMAGE_JPEG
            }

            assetRepository.save(assetToModify)
            val result = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset

            assertNotNull(result)
            assertEquals("my new name", result.name)
            assertEquals(MediaType.IMAGE_JPEG, result.mediaType)
            assertNotEquals(
                result.auditFields.lastModifiedOn.truncateNanos(),
                result.auditFields.createdOn.truncateNanos()
            )
        }
    }

}
