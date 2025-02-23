package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFiles


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseAllAssetsView(
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    viewModel: LightboxViewModel
) {
    val showInformation = mutableStateOf(false)

    fun toggleShowInformation() {
        showInformation.value = !showInformation.value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Browse all")
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
                    IconButton(onClick = { toggleShowInformation() }) {
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
                        LightboxInformationPaneScaffold(
                            viewModel.assets,
                            viewModel.selectedAssets.collectAsState().value,
                            showInformation.value,
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
fun LightboxInformationPaneScaffold(
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
            AnimatedPane {
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
