package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceView(
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    viewModel: LightboxViewModel,
) {
    val showInformation = mutableStateOf(false)
    val showViewSettingsDialog = remember { mutableStateOf(false) }
    val selectedAssets = viewModel.selectedAssets.collectAsState()
    val configuration = viewModel.configuration.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setStatus(AssetStatus.ORPHANED_FILE)
    }

    val assets = viewModel.pagedAssets.collectAsLazyPagingItems()

    if (showViewSettingsDialog.value) {
        LightboxViewSettingsDialog(
            showAssetFilenameCaption = configuration.value.uiConfiguration.lightbox.showAssetFilenameCaption,
            thumbnailZoomPercent = configuration.value.uiConfiguration.lightbox.thumbnailZoomPercent,
            onShowAssetFilenameCaptionChange = { viewModel.setShowAssetFilenameCaption(it) },
            onThumbnailZoomPercentChange = { viewModel.setThumbnailZoomPercent(it) },
            onDismiss = { showViewSettingsDialog.value = false },
        )
    }

    fun toggleShowInformation() {
        showInformation.value = !showInformation.value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Orphaned files")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.setStatus(null)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showViewSettingsDialog.value = true }
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Open view settings"
                        )
                    }
                    IconButton(
                        enabled = selectedAssets.value.isNotEmpty(),
                        onClick = { toggleShowInformation() })
                    {
                        Icon(
                            if (showInformation.value) Icons.Filled.Info else Icons.Outlined.Info,
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
        }
    )
}
