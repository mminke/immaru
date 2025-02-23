package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.window.core.layout.WindowWidthSizeClass
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.lightbox.LightboxDestinations.BROWSE
import com.earthrevealed.immaru.lightbox.LightboxDestinations.BY_DATE
import com.earthrevealed.immaru.lightbox.LightboxDestinations.PEOPLE
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

enum class LightboxDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    BROWSE("Browse", Icons.Filled.Home, "Browse all media"),
    BY_DATE("By date", Icons.Filled.CalendarViewMonth, "View by month"),
    PEOPLE("People", Icons.Filled.People, "By people"),
}

@Composable
fun LightboxScreen(
    collection: Collection,
    onNavigateBack: () -> Unit,
    onViewAsset: (Asset) -> Unit,
    onAssetsSelected: (List<Asset>) -> Unit,
    viewModel: LightboxViewModel = koinViewModel { parametersOf(collection) },
) {
    val currentDestination = rememberSaveable { mutableStateOf(BROWSE) }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            NavigationSuiteType.NavigationDrawer
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }

    NavigationSuiteScaffold(
        layoutType = customNavSuiteType,
        navigationSuiteItems = {
            LightboxDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.contentDescription) },
                    label = { Text(it.label) },
                    selected = it == currentDestination.value,
                    onClick = { currentDestination.value = it }
                )
            }
        }
    ) {
        when (currentDestination.value) {
            BROWSE -> BrowseAllAssetsView(onNavigateBack, onViewAsset, viewModel)
            BY_DATE -> BrowseByDateView(collection, onNavigateBack)
            PEOPLE -> Column { Text("People") }
        }
    }
}
