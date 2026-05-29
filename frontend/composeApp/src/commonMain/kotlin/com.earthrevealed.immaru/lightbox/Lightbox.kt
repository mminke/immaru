package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.earthrevealed.immaru.assets.Asset

@Composable
fun Lightbox(
    assets: LazyPagingItems<Asset>,
    selectedAssets: List<Asset>,
    onAssetClicked: (Asset) -> Unit,
    onAssetDoubleClicked: (Asset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    Box(modifier = modifier) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .testTag("lightbox-grid"),
            state = gridState,
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(
                count = assets.itemCount,
                key = { index -> assets[index]?.id?.toString() ?: "placeholder_$index" },
            ) { index ->
                val asset = assets[index]
                if (asset != null) {
                    AssetThumbnail(
                        asset,
                        selected = selectedAssets.contains(asset),
                        onClick = onAssetClicked,
                        onDoubleClick = onAssetDoubleClicked,
                        onLongClick = { println("long clicked ${asset}") },
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                }
            }
        }

        if (assets.loadState.prepend is LoadState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (assets.loadState.append is LoadState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
