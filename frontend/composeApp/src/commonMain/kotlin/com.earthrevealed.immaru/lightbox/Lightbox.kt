package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.assets.Asset


@Composable
fun Lightbox(
    assets: List<Asset>,
    selectedAssets: List<Asset>,
    onAssetClicked: (Asset) -> Unit,
    onAssetDoubleClicked: (Asset) -> Unit,
    hasMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    val nearEnd by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 5
        }
    }

    LaunchedEffect(nearEnd, hasMore, isLoadingMore) {
        if (nearEnd && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(assets) { asset ->
            AssetThumbnail(
                asset,
                selected = selectedAssets.contains(asset),
                onClick = { asset -> onAssetClicked(asset) },
                onDoubleClick = { asset -> onAssetDoubleClicked(asset) },
                onLongClick = { asset -> println("long clicked ${asset}") },
            )
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
