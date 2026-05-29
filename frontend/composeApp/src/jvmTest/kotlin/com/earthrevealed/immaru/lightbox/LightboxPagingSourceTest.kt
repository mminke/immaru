package com.earthrevealed.immaru.lightbox

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetPage
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssetPagingSourceTest {

    @Test
    fun `refresh append prepend keeps paging window deterministic and unique`() = runBlocking {
        val collectionId = CollectionId()
        val allAssets = createAssets(collectionId, 15)
        val repository = FakeAssetRepository(allAssets)
        val source = AssetPagingSource(collectionId, repository)

        val refreshResult = source.load(
            LoadParams.Refresh(
                key = null,
                loadSize = 5,
                placeholdersEnabled = false,
            )
        )
        val refreshPage = assertPage(refreshResult)
        assertEquals((0 until 5).map { "Asset $it" }, refreshPage.data.map { it.name })
        assertNull(refreshPage.prevKey)
        assertEquals(PageDirection.FORWARD, refreshPage.nextKey?.direction)

        val appendResult = source.load(
            LoadParams.Append(
                key = assertNotNull(refreshPage.nextKey),
                loadSize = 5,
                placeholdersEnabled = false,
            )
        )
        val appendPage = assertPage(appendResult)
        assertEquals((5 until 10).map { "Asset $it" }, appendPage.data.map { it.name })
        assertEquals(PageDirection.BACKWARD, appendPage.prevKey?.direction)

        val prependResult = source.load(
            LoadParams.Prepend(
                key = assertNotNull(appendPage.prevKey),
                loadSize = 5,
                placeholdersEnabled = false,
            )
        )
        val prependPage = assertPage(prependResult)
        assertEquals((0 until 5).map { "Asset $it" }, prependPage.data.map { it.name })
        assertTrue(prependPage.data.map { it.id }.distinct().size == prependPage.data.size)
    }

    @Test
    fun `getRefreshKey points to anchor item cursor`() {
        val collectionId = CollectionId()
        val assets = createAssets(collectionId, 8)
        val source = AssetPagingSource(collectionId, FakeAssetRepository(assets))

        val state = PagingState<AssetPagingKey, Asset>(
            pages = listOf(
                LoadResult.Page(
                    data = assets,
                    prevKey = null,
                    nextKey = null,
                )
            ),
            anchorPosition = 3,
            config = PagingConfig(pageSize = 5),
            leadingPlaceholderCount = 0,
        )

        val refreshKey = source.getRefreshKey(state)
        assertNotNull(refreshKey)
        assertEquals(PageDirection.FORWARD, refreshKey.direction)
        assertEquals(assets[3].id, refreshKey.cursor.id)
    }

    private fun assertPage(
        result: LoadResult<AssetPagingKey, Asset>
    ): LoadResult.Page<AssetPagingKey, Asset> {
        return when (result) {
            is LoadResult.Page -> result
            is LoadResult.Error -> throw AssertionError("Expected page, got error", result.throwable)
            is LoadResult.Invalid -> throw AssertionError("Expected page, got invalid")
        }
    }

    private fun createAssets(collectionId: CollectionId, count: Int): List<Asset> {
        return List(count) { index ->
            FileAsset(
                collectionId = collectionId,
                originalFilename = "Asset $index",
            )
        }
    }

    private class FakeAssetRepository(
        private val allAssets: List<Asset>,
    ) : AssetRepository {
        override suspend fun findById(
            collectionId: CollectionId,
            assetId: com.earthrevealed.immaru.assets.AssetId
        ): Asset? {
            return allAssets.firstOrNull { it.collectionId == collectionId && it.id == assetId }
        }

        override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
            return allAssets.filter { it.collectionId == collectionId }
        }

        override suspend fun save(asset: Asset) = Unit

        override suspend fun delete(id: com.earthrevealed.immaru.assets.AssetId) = Unit

        override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> = emptyFlow()

        override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) = Unit

        override suspend fun findSelectableDates(collectionId: CollectionId): List<com.earthrevealed.immaru.assets.SelectableYear> {
            return emptyList()
        }

        override suspend fun findPageFor(
            collectionId: CollectionId,
            limit: Int,
            cursor: AssetCursor?,
            direction: PageDirection,
        ): AssetPage {
            if (collectionId != allAssets.firstOrNull()?.collectionId) {
                return AssetPage(emptyList(), nextCursor = null, prevCursor = null, hasMore = false)
            }

            return try {
                when (direction) {
                    PageDirection.FORWARD -> forwardPage(limit, cursor)
                    PageDirection.BACKWARD -> backwardPage(limit, cursor)
                }
            } catch (throwable: Throwable) {
                throw AssetRetrievalException(throwable)
            }
        }

        private fun forwardPage(limit: Int, cursor: AssetCursor?): AssetPage {
            val startIndex = cursor?.let { indexOfCursor(it) + 1 } ?: 0
            val endExclusive = (startIndex + limit).coerceAtMost(allAssets.size)
            val items = if (startIndex >= allAssets.size) {
                emptyList()
            } else {
                allAssets.subList(startIndex, endExclusive)
            }
            return buildPage(items, startIndex, endExclusive)
        }

        private fun backwardPage(limit: Int, cursor: AssetCursor?): AssetPage {
            val endExclusive = cursor?.let { indexOfCursor(it) } ?: allAssets.size
            val startIndex = (endExclusive - limit).coerceAtLeast(0)
            val items = if (startIndex >= endExclusive) {
                emptyList()
            } else {
                allAssets.subList(startIndex, endExclusive)
            }
            return buildPage(items, startIndex, endExclusive)
        }

        private fun buildPage(items: List<Asset>, startIndex: Int, endExclusive: Int): AssetPage {
            val prevCursor = if (startIndex > 0 && items.isNotEmpty()) cursorFor(items.first()) else null
            val nextCursor = if (endExclusive < allAssets.size && items.isNotEmpty()) cursorFor(items.last()) else null
            val hasMore = endExclusive < allAssets.size || startIndex > 0
            return AssetPage(
                items = items,
                nextCursor = nextCursor,
                prevCursor = prevCursor,
                hasMore = hasMore,
            )
        }

        private fun cursorFor(asset: Asset): AssetCursor {
            return AssetCursor(
                createdAt = asset.auditFields.createdOn,
                id = asset.id,
            )
        }

        private fun indexOfCursor(cursor: AssetCursor): Int {
            val index = allAssets.indexOfFirst { it.id == cursor.id }
            check(index >= 0) { "Cursor ${cursor.id} is not present in fake repository" }
            return index
        }
    }
}
