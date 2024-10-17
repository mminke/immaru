package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.common.ErrorMessage
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    viewModel: LightboxViewModel,
    onNavigateBack: () -> Unit
) {
    val filePicker = rememberFilePickerLauncher(
        type = PickerType.ImageAndVideo,
        mode = PickerMode.Multiple()
    ) { files ->
        println("FILES SIZE: ${files?.size}")
        files?.forEach { file ->
            println("FILE SELECTED: ${file.name} [size=${file.getSize()}, $file]")

            // Create the asset
//            file.name
//            val size: Long = file.getSize()

            viewModel.createAssetFor(file)

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Immaru")
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
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        viewModel.assets.value.forEach { asset ->
                            AssetThumbnail(asset)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = { filePicker.launch() }
            ) {
                Icon(Icons.Filled.Add, "Add a assets.")
            }
        }
    )
}

@Composable
fun AssetThumbnail(
    asset: Asset,
) {
    Box(
        contentAlignment = Alignment.BottomStart
    ) {
        if (asset is FileAsset) {
            AsyncImage(
                model = asset.contentUrl,
                contentDescription = null,
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