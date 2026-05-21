package com.earthrevealed.immaru

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.earthrevealed.immaru.asset.AssetScreen
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.collections.CollectionDetailsScreen
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionsScreen
import com.earthrevealed.immaru.common.HttpClientProvider
import com.earthrevealed.immaru.configuration.Configuration
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import com.earthrevealed.immaru.configuration.ConfigurationScreen
import com.earthrevealed.immaru.lightbox.LightboxScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object ConfigurationRoute

@Serializable
data object CollectionsRoute

@Serializable
data object NewCollectionRoute

@Serializable
data class AssetRoute(
    val collectionId: String,
    val assetId: String,
)

@Serializable
data class LightboxRoute(
    val collectionId: String,
)

@Serializable
data class CollectionDetailsRoute(
    val collectionId: String,
)

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
) {
    val httpClientProvider = koinInject<HttpClientProvider>()
    val configurationRepository = koinInject<ConfigurationRepository>()
    val httpClient = httpClientProvider.httpClient.collectAsState()

    val startDestination = if (httpClient.value == null) ConfigurationRoute else CollectionsRoute

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable<ConfigurationRoute> {
                val configuration = configurationRepository.configuration.collectAsState(Configuration())

                ConfigurationScreen(
                    configuration.value,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<CollectionsRoute> {
                CollectionsScreen(
                    onCollectionSelected = {
                        navController.navigate(LightboxRoute(it.id.toString()))
                    },
                    onCollectionInfo = {
                        navController.navigate(CollectionDetailsRoute(it.id.toString()))
                    },
                    onNewCollection = { navController.navigate(NewCollectionRoute) },
                    onOpenConfiguration = { navController.navigate(ConfigurationRoute) }
                )
            }
            composable<LightboxRoute> { backStackEntry ->
                val lightboxRoute = runCatching { backStackEntry.toRoute<LightboxRoute>() }.getOrNull()
                val collectionId = lightboxRoute?.collectionId?.let {
                    runCatching { CollectionId.fromString(it) }.getOrNull()
                }

                if (collectionId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                LightboxScreen(
                    collectionId,
                    onNavigateBack = { navController.popBackStack() },
                    onViewAsset = { asset ->
                        navController.navigate(
                            AssetRoute(
                                collectionId = asset.collectionId.value.toString(),
                                assetId = asset.id.value.toString(),
                            )
                        )
                    },
                    onAssetsSelected = { (_) -> Unit },
                )
            }
            composable<CollectionDetailsRoute> { backStackEntry ->
                val collectionRoute = runCatching { backStackEntry.toRoute<CollectionDetailsRoute>() }.getOrNull()
                val collectionId = collectionRoute?.collectionId?.let {
                    runCatching { CollectionId.fromString(it) }.getOrNull()
                }

                if (collectionId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                CollectionDetailsScreen(
                    collectionId = collectionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<NewCollectionRoute> {
                CollectionDetailsScreen(
                    isNew = true,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<AssetRoute> { backStackEntry ->
                val assetRoute = runCatching { backStackEntry.toRoute<AssetRoute>() }.getOrNull()

                val collectionId = assetRoute?.collectionId?.let {
                    runCatching { CollectionId.fromString(it) }.getOrNull()
                }
                val assetId = assetRoute?.assetId?.let {
                    runCatching { AssetId.fromString(it) }.getOrNull()
                }

                if (collectionId == null || assetId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                AssetScreen(
                    collectionId = collectionId,
                    assetId = assetId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun MainNavigationPreview() {
    MainNavigation()
}
