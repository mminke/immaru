package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.common.ErrorMessage
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFiles
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    collection: Collection,
    viewModel: LightboxViewModel = koinViewModel { parametersOf(collection) },
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    onAssetsSelected: (List<Asset>) -> Unit,
) {
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
            Box(
                modifier = Modifier.consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                if (viewModel.isLoading.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp)
                        ) {
                            items(viewModel.assets.value) { asset ->
                                AssetThumbnail(
                                    asset,
                                    onClick = { asset -> onViewAsset(asset) },
                                    onDoubleClick = { asset -> println("double clicked ${asset}") },
                                    onLongClick = { asset -> println("long clicked ${asset}") },
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            UploadFloatingActionButtons(
                onFilesPicked = { files ->
                    files.forEach { file ->
                        viewModel.createAssetFor(file)
                    }
                },
                onDirectoryPicked = { directory ->
                    viewModel.createAssetsFor(directory)
                }
            )
        }
    )
}

@Composable
fun UploadFloatingActionButtons(
    onFilesPicked: (PlatformFiles) -> Unit,
    onDirectoryPicked: (PlatformDirectory) -> Unit
) {
    val showSmallButtons = remember { mutableStateOf(false) }

    val filePicker = rememberFilePickerLauncher(
        title = "Select file(s)",
        type = PickerType.ImageAndVideo,
        mode = PickerMode.Multiple()
    ) { files ->
        if (files != null) onFilesPicked(files)
        showSmallButtons.value = false
    }
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Select folder"
    ) { directory ->
        if (directory != null) onDirectoryPicked(directory)
        showSmallButtons.value = false
    }

    if (FileKit.isDirectoryPickerSupported()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showSmallButtons.value) {
                SmallFloatingActionButton(
                    modifier = Modifier.padding(bottom = 4.dp),
                    onClick = { filePicker.launch() }
                ) {
                    Icon(Icons.Filled.UploadFile, "Add a file.")
                }
                SmallFloatingActionButton(
                    modifier = Modifier.padding(bottom = 12.dp),
                    onClick = { directoryPicker.launch() }
                ) {
                    Icon(Icons.Filled.DriveFolderUpload, "Add a folder.")
                }
            }
            FloatingActionButton(
                onClick = {
                    showSmallButtons.value = !showSmallButtons.value
                }
            ) {
                Icon(Icons.Filled.Add, "Add assets.")
            }
        }
    } else {
        FloatingActionButton(
            onClick = { filePicker.launch() }
        ) {
            Icon(Icons.Filled.Add, "Add assets.")
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetThumbnail(
    asset: Asset,
    onClick: (Asset) -> Unit,
    onDoubleClick: (Asset) -> Unit,
    onLongClick: (Asset) -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .background(Color.Red)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { onClick(asset) },
                onDoubleClick = { onDoubleClick(asset) },
                onLongClick = { onLongClick(asset) }
            )
    ) {
        if (asset is FileAsset) {
            AsyncImage(
                model = asset.contentUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onError = { println("Something went wrong loading the image: ${it.result.throwable}")}

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