package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.assets.Asset


@Composable
fun Lightbox(
    assets: List<Asset>,
    selectedAssets: List<Asset>,
    onAssetClicked: (Asset) -> Unit,
    onAssetDoubleClicked: (Asset) -> Unit
) {
    LazyVerticalGrid(
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
    }
}
