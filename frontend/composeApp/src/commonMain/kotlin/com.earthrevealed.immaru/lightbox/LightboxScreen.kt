package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.earthrevealed.immaru.asset.AssetViewModel
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import com.earthrevealed.immaru.common.Table
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFiles
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    collection: Collection,
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    onAssetsSelected: (List<Asset>) -> Unit,
    viewModel: LightboxViewModel = koinViewModel { parametersOf(collection) },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Immaru")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowInformation() }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Show Information"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                if (viewModel.isLoading.value) {
                    CenteredProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        LightboxSupportingPane(
                            viewModel.assets,
                            viewModel.selectedAssets.collectAsState().value,
                            viewModel.showInformation.value,
                            onAssetClicked = onViewAsset,
                            onAssetDoubleClicked = { viewModel.toggleAssetSelected(it) },
                        )
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LightboxSupportingPane(
    assets: List<Asset>,
    selectedAssets: List<Asset>,
    showInformation: Boolean,
    onAssetClicked: (Asset) -> Unit,
    onAssetDoubleClicked: (Asset) -> Unit,
) {
    val navigator = rememberSupportingPaneScaffoldNavigator(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()).let {
            if (!showInformation) {
                it.copy(maxHorizontalPartitions = 1)
            } else {
                it
            }
        }
    )

    if (!showInformation) {
        navigator.navigateTo(ThreePaneScaffoldRole.Primary)
    } else {
        navigator.navigateTo(ThreePaneScaffoldRole.Secondary)
    }

    SupportingPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        mainPane = {
            AnimatedPane(modifier = Modifier.safeContentPadding()) {
                Lightbox(
                    assets,
                    selectedAssets,
                    onAssetClicked = onAssetClicked,
                    onAssetDoubleClicked = onAssetDoubleClicked
                )
            }
        },
        supportingPane = {
            AnimatedPane {
                AssetInformation(selectedAssets.lastOrNull())
            }
        },
    )
}

@Composable
fun AssetInformationTable(asset: Asset?) {
    if (asset == null) {
        Text("Nothing selected")
    } else {
        val properties = mutableListOf(
            "Name" to asset.name,
        ).apply {
            if (asset is FileAsset) {
                add("Original filename" to asset.originalFilename)
                add("Media type" to asset.mediaType.toString())
            }
        }

        Column(modifier = Modifier.padding(5.dp)) {
            Text("Information")

            Table(
                properties.size,
                2,
                modifier = Modifier.border(1.dp, Color.LightGray).fillMaxWidth()
            ) { row, column ->
                if (column == 0) {
                    Text(
                        properties[row].first,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        properties[row].second,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun AssetInformation(asset: Asset?) {
    if (asset == null) {
        Text("Nothing selected")
    } else {
        val properties = mutableListOf(
            "Name" to asset.name,
        ).apply {
            if (asset is FileAsset) {
                add("Original filename" to asset.originalFilename)
                add("Media type" to asset.mediaType.toString())
            }
        }

        Column(modifier = Modifier.padding(5.dp)) {
            Text("Information")

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
                    .padding(5.dp)
            ) {
                items(properties) { item ->
                    Row {
                        Text(
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth(0.3f),
                            text = item.first,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            item.second, modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth(1f)
                        )
                    }
                }
            }
        }
    }
}

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
                onError = { println("ERROR: Something went wrong loading the image: ${it.result.throwable}") }
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