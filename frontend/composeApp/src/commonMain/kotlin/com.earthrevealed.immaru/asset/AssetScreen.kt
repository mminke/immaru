package com.earthrevealed.immaru.asset

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.lightbox.contentUrl


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    asset: Asset,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Immaru - ${asset.name}")
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier.consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                Asset(
                    asset
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Asset(
    asset: Asset,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .background(Color.Red)
            .aspectRatio(1f)
    ) {
        if (asset is FileAsset) {
            AsyncImage(
                model = asset.contentUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()

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