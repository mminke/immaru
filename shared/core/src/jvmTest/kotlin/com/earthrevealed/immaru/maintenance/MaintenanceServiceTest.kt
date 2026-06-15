package com.earthrevealed.immaru.maintenance

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetPage
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.assets.SelectableYear
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.io.files.Path
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MaintenanceServiceTest {

    @Test
    fun `create assets for orphaned files stores ORPHANED_FILE assets with parsed ids`() =
        withTemporaryLibrary { libraryPath ->
            val orphanedAssetId = AssetId()
            Files.createDirectories(libraryPath)
            Files.writeString(libraryPath.resolve("${orphanedAssetId.value}.jpg"), "content")

            val repository = InMemoryAssetRepository()
            val service = MaintenanceService(
                library = Library(Path(libraryPath.toString())),
                assetRepository = repository,
            )

            val collectionId = CollectionId()
            val createdAssetIds = service.createAssetsForOrphanedFiles(collectionId).toList()

            assertEquals(listOf(orphanedAssetId), createdAssetIds)
            assertEquals(1, repository.savedAssets.size)

            val saved = repository.savedAssets.single() as FileAsset
            assertEquals(orphanedAssetId, saved.id)
            assertEquals(collectionId, saved.collectionId)
            assertEquals("orphaned", saved.name)
            assertEquals("orphaned", saved.originalFilename)
            assertEquals(AssetStatus.ORPHANED_FILE, saved.status)
            assertNotNull(saved.mediaType)
            assertNotNull(saved.originalCreatedAt)
            assertNotNull(saved.contentHash)
            assertTrue(saved.contentHash!!.isNotEmpty())
        }

    private fun withTemporaryLibrary(block: suspend (java.nio.file.Path) -> Unit) {
        kotlinx.coroutines.runBlocking {
            val tempDir = Files.createTempDirectory("maintenance-service-test")
            try {
                block(tempDir)
            } finally {
                tempDir.toFile().deleteRecursively()
            }
        }
    }

    private class InMemoryAssetRepository : AssetRepository {
        val savedAssets = mutableListOf<Asset>()

        override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? = null

        override suspend fun findAllFor(collectionId: CollectionId): List<Asset> = emptyList()

        override suspend fun save(asset: Asset) {
            savedAssets += asset
        }

        override suspend fun delete(id: AssetId) {
        }

        override suspend fun assetExists(assetId: AssetId): Boolean =
            savedAssets.any { it.id == assetId }

        override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> = emptyFlow()

        override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        }

        override suspend fun findSelectableDates(collectionId: CollectionId): List<SelectableYear> = emptyList()

        override suspend fun findPageFor(
            collectionId: CollectionId,
            limit: Int,
            cursor: AssetCursor?,
            direction: PageDirection,
        ): AssetPage = AssetPage(
            items = emptyList(),
            nextCursor = null,
            prevCursor = null,
            hasMore = false,
        )
    }
}
