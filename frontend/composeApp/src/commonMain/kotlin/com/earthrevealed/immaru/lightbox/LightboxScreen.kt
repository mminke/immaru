package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import com.earthrevealed.immaru.lightbox.LightboxDestination.BROWSE
import com.earthrevealed.immaru.lightbox.LightboxDestination.BY_DATE
import com.earthrevealed.immaru.lightbox.LightboxDestination.MAINTENANCE
import com.earthrevealed.immaru.lightbox.LightboxDestination.PEOPLE
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

enum class LightboxDestination(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    BROWSE("Browse", Icons.Filled.Home, "Browse all media"),
    BY_DATE("By date", Icons.Filled.CalendarViewMonth, "View by month"),
    PEOPLE("People", Icons.Filled.People, "By people"),
    MAINTENANCE("Maintenance", Icons.Filled.Handyman, "Maintenance tools"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    collectionId: CollectionId,
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    onAssetsSelected: (List<Asset>) -> Unit,
    viewModel: LightboxViewModel = koinViewModel { parametersOf(collectionId) },
) {
    if (viewModel.isLoading.value && viewModel.collection.value == null) {
        CenteredProgressIndicator()
        return
    }

    if (viewModel.errorMessage.value.isNotBlank()) {
        ErrorMessage(viewModel.errorMessage.value)
        return
    }

    val collection = viewModel.collection.value
    if (collection == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    val currentDestination = rememberSaveable { mutableStateOf(BROWSE) }
    val topAppBarState by viewModel.topAppBarState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection.name) },
                actions = {
                    topAppBarState.actions.forEach { action ->
                        IconButton(onClick = action.onClick, enabled = action.enabled) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription
                            )
                        }
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { onNavigateBack() }
                            .padding(16.dp)
                    )
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
        ) {
            NavigationSuiteScaffold(
                modifier = Modifier
                    .padding(8.dp),
                layoutType = determineNavigationSuite(currentWindowAdaptiveInfo()),
                navigationSuiteItems = {
                    LightboxDestination.entries.forEach {
                        item(
                            icon = { Icon(it.icon, contentDescription = it.contentDescription) },
                            label = { Text(it.label) },
                            selected = it == currentDestination.value,
                            onClick = { currentDestination.value = it },
                            modifier = Modifier
                                .padding(end = 8.dp)
                        )
                    }
                }
            ) {
                when (currentDestination.value) {
                    BROWSE -> BrowseAllAssetsView(onNavigateBack, onViewAsset, viewModel)
                    BY_DATE -> BrowseByDateView(collection, onNavigateBack, viewModel)
                    MAINTENANCE -> MaintenanceView(onNavigateBack, onViewAsset, viewModel)
                    PEOPLE -> Column { Text("People") }
                }
            }
        }
    }
}

private fun determineNavigationSuite(adaptiveInfo: WindowAdaptiveInfo) = with(adaptiveInfo) {
//    if (showPermanantNavigationDrawer()) {
//        NavigationSuiteType.NavigationRail
//    } else {
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
//    }
}

private fun WindowAdaptiveInfo.showPermanantNavigationDrawer(): Boolean =
    windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
