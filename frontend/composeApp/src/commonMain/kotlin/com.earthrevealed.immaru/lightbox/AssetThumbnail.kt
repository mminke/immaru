package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.earthrevealed.immaru.asset.AssetViewModel
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import org.koin.compose.koinInject


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetThumbnail(
    asset: Asset,
    selected: Boolean,
    onClick: (Asset) -> Unit,
    onDoubleClick: (Asset) -> Unit,
    onLongClick: (Asset) -> Unit,
    assetViewModel: AssetViewModel = koinInject(),
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (selected) Color.Blue else Color.White
            )
            .combinedClickable(
                onClick = { onClick(asset) },
                onDoubleClick = { onDoubleClick(asset) },
                onLongClick = { onLongClick(asset) }
            )
    ) {
        if (asset is FileAsset) {
            val contentUrl = assetViewModel.contentUrlForAsset(asset).collectAsState(null)

            AsyncImage(
                model = contentUrl.value,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onError = { println("ERROR: Something went wrong loading the image: ${it.result.throwable} [asset.id=${asset.id}]") }
            )
        } else {
            TODO("Not a file asset, define an image placeholder")
        }

        Box(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = 0.5f))
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column {
                Text(asset.name, color = Color.White)
            }
        }
    }
}