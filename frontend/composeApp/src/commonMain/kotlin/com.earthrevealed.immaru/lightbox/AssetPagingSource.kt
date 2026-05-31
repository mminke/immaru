package com.earthrevealed.immaru.lightbox

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.collections.CollectionId

class AssetPagingSource(
    private val collectionId: CollectionId,
    private val assetRepository: AssetRepository,
) : PagingSource<AssetPagingKey, Asset>() {

    override suspend fun load(params: LoadParams<AssetPagingKey>): LoadResult<AssetPagingKey, Asset> {
        val (direction, cursor) = when (params) {
            is LoadParams.Refresh -> PageDirection.FORWARD to null
            is LoadParams.Append -> params.key.direction to params.key.cursor
            is LoadParams.Prepend -> params.key.direction to params.key.cursor
        }

        return try {
            println("Loading page...")
            val page = assetRepository.findPageFor(
                collectionId = collectionId,
                limit = params.loadSize.coerceAtMost(MAX_PAGE_SIZE),
                cursor = cursor,
                direction = direction,
            )

            val forwardKey = page.nextCursor?.let { AssetPagingKey(it, PageDirection.FORWARD) }
            val backwardKey = page.prevCursor?.let { AssetPagingKey(it, PageDirection.BACKWARD) }

            val prevKey = when (params) {
                is LoadParams.Refresh -> null
                is LoadParams.Prepend -> if (page.hasMore) backwardKey else null
                is LoadParams.Append -> backwardKey
            }
            val nextKey = when (params) {
                is LoadParams.Prepend -> forwardKey
                is LoadParams.Append -> if (page.hasMore) forwardKey else null
                is LoadParams.Refresh -> if (page.hasMore) forwardKey else null
            }

            LoadResult.Page(
                data = page.items,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (exception: AssetRetrievalException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<AssetPagingKey, Asset>): AssetPagingKey? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorItem = state.closestItemToPosition(anchorPosition) ?: return null
        return AssetPagingKey(
            cursor = AssetCursor(
                createdAt = anchorItem.auditFields.createdAt,
                id = anchorItem.id,
            ),
            direction = PageDirection.FORWARD,
        )
    }

    private companion object {
        private const val MAX_PAGE_SIZE = 20
    }
}

data class AssetPagingKey(
    val cursor: AssetCursor,
    val direction: PageDirection,
)
