package com.earthrevealed.immaru.lightbox

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseAllAssetsView(
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    viewModel: LightboxViewModel
) {
    val showInformation = remember { mutableStateOf(false) }
    val showViewSettingsDialog = remember { mutableStateOf(false) }
    val selectedAssets = viewModel.selectedAssets.collectAsState()
    val assets = viewModel.pagedAssets.collectAsLazyPagingItems()
    val configuration = viewModel.configuration.collectAsState()

    fun toggleShowInformation() {
        showInformation.value = !showInformation.value
    }

    LaunchedEffect(showViewSettingsDialog.value, showInformation.value, selectedAssets.value.isNotEmpty()) {
        viewModel.setStatus(null)
        viewModel.updateTopAppBarState(
            TopAppBarState(
                title = "Browse all",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = { onNavigateBack() },
                actions = listOf(
                    TopAppBarAction(
                        icon = Icons.Filled.Settings,
                        onClick = { showViewSettingsDialog.value = true },
                        contentDescription = "Open view settings"
                    ),
                    TopAppBarAction(
                        enabled = selectedAssets.value.isNotEmpty(),
                        icon = if (showInformation.value.also { println("icon show info value $it") }) Icons.Filled.Info else Icons.Outlined.Info,
                        onClick = { toggleShowInformation() },
                        contentDescription = "Show information about selected assets"
                    )
                )
            )
        )
    }

    if (showViewSettingsDialog.value) {
        LightboxViewSettingsDialog(
            showAssetFilenameCaption = configuration.value.uiConfiguration.lightbox.showAssetFilenameCaption,
            thumbnailZoomPercent = configuration.value.uiConfiguration.lightbox.thumbnailZoomPercent,
            onShowAssetFilenameCaptionChange = { viewModel.setShowAssetFilenameCaption(it) },
            onThumbnailZoomPercentChange = { viewModel.setThumbnailZoomPercent(it) },
            onDismiss = { showViewSettingsDialog.value = false },
        )
    }

    Scaffold(
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
                    } else if (assets.loadState.refresh is LoadState.Error) {
                        ErrorMessage("Cannot retrieve assets!")
                    } else if (assets.loadState.refresh is LoadState.Loading) {
                        CenteredProgressIndicator()
                    } else {
                        LightboxInformationPaneScaffold(
                            assets,
                            selectedAssets.value,
                            if (selectedAssets.value.isEmpty()) false else showInformation.value,
                            showAssetFilenameCaption = configuration.value.uiConfiguration.lightbox.showAssetFilenameCaption,
                            thumbnailZoomPercent = configuration.value.uiConfiguration.lightbox.thumbnailZoomPercent,
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
    assets: LazyPagingItems<Asset>,
    selectedAssets: List<Asset>,
    showInformation: Boolean,
    showAssetFilenameCaption: Boolean,
    thumbnailZoomPercent: Int,
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

    LaunchedEffect(showInformation) {
        if (!showInformation) {
            navigator.navigateTo(ThreePaneScaffoldRole.Primary)
        } else {
            navigator.navigateTo(ThreePaneScaffoldRole.Secondary)
        }
    }

    SupportingPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        mainPane = {
            AnimatedPane(
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
            ) {
                Lightbox(
                    assets,
                    selectedAssets,
                    showAssetFilenameCaption = showAssetFilenameCaption,
                    thumbnailZoomPercent = thumbnailZoomPercent,
                    onAssetClicked = onAssetClicked,
                    onAssetDoubleClicked = onAssetDoubleClicked,
                )
            }
        },
        supportingPane = {
            AnimatedPane(
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
            ) {
                AssetInformation(selectedAssets.lastOrNull())
            }
        },
    )
}

@Composable
fun UploadFloatingActionButtons(
    onFilesPicked: (List<PlatformFile>) -> Unit,
    onDirectoryPicked: (PlatformFile) -> Unit
) {
    val showSmallButtons = remember { mutableStateOf(false) }

    val filePicker = rememberFilePickerLauncher(
        type = FileKitType.ImageAndVideo,
        mode = FileKitMode.Multiple(),
    ) { files ->
        if (files != null) onFilesPicked(files)
        showSmallButtons.value = false
    }
    val directoryPicker = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) onDirectoryPicked(directory)
        showSmallButtons.value = false
    }

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
}
