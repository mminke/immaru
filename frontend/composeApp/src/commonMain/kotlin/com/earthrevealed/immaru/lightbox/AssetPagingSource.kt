package com.earthrevealed.immaru.lightbox

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.collections.CollectionId

class AssetPagingSource(
    private val collectionId: CollectionId,
    private val assetRepository: AssetRepository,
    private val status: AssetStatus? = null,
    private val searchQuery: String? = null,
) : PagingSource<AssetCursor, Asset>() {

    override suspend fun load(params: LoadParams<AssetCursor>): LoadResult<AssetCursor, Asset> {
        val cursor = params.key
        val direction = when (params) {
            is LoadParams.Append -> PageDirection.FORWARD.also { println("Append") }
            is LoadParams.Prepend -> PageDirection.BACKWARD.also { println("Prepend") }
            is LoadParams.Refresh -> PageDirection.FORWARD.also { println("Refresh") }
        }

        return try {
            val page = assetRepository.findPageFor(
                collectionId = collectionId,
                limit = params.loadSize,
                cursor = cursor,
                direction = direction,
                status = status,
                searchQuery = searchQuery,
            )

            LoadResult.Page(
                data = page.items,
                prevKey = page.prevCursor,
                nextKey = page.nextCursor,
            )
        } catch (exception: AssetRetrievalException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<AssetCursor, Asset>): AssetCursor? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }
}
