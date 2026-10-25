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
import androidx.compose.material3.Scaffold
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

    fun toggleShowInformation() {
        showInformation.value = !showInformation.value
    }

    LaunchedEffect(showViewSettingsDialog.value, showInformation.value, selectedAssets.value.isNotEmpty()) {
        viewModel.setStatus(AssetStatus.ORPHANED_FILE)

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
                        icon = if (showInformation.value) Icons.Filled.Info else Icons.Outlined.Info,
                        onClick = { toggleShowInformation() },
                        contentDescription = "Show information about selected assets"
                    )
                )
            )
        )
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
        }
    )
}
