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
import com.earthrevealed.immaru.asset.AssetDetailsViewModel
import com.earthrevealed.immaru.asset.AssetScreen
import com.earthrevealed.immaru.asset.AssetViewModel
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.*
import com.earthrevealed.immaru.collections.repositories.KtorCollectionRepository
import com.earthrevealed.immaru.common.HttpClientProvider
import com.earthrevealed.immaru.configuration.Configuration
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import com.earthrevealed.immaru.configuration.ConfigurationScreen
import com.earthrevealed.immaru.configuration.ConfigurationViewModel
import com.earthrevealed.immaru.configuration.datastore.DataStoreConfigurationRepository
import com.earthrevealed.immaru.coroutines.DispatcherProvider
import com.earthrevealed.immaru.lightbox.BrowseByDateViewViewModel
import com.earthrevealed.immaru.lightbox.LightboxScreen
import com.earthrevealed.immaru.lightbox.LightboxViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

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
val appModule = module {
    singleOf(::DataStoreConfigurationRepository) { bind<ConfigurationRepository>() }

    singleOf(::ImmaruHttpClientProvider) { bind<HttpClientProvider>() }

    singleOf(::KtorCollectionRepository) { bind<CollectionRepository>() }
    singleOf(::KtorAssetRepository) { bind<AssetRepository>() }

    viewModelOf(::ConfigurationViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModel { params ->
        CollectionDetailsViewModel(
            collectionRepository = get(),
            collectionId = params.getOrNull()
        )
    }
    viewModelOf(::LightboxViewModel)
    viewModelOf(::AssetDetailsViewModel)
    viewModelOf(::AssetViewModel)
    viewModelOf(::BrowseByDateViewViewModel)
}

class ImmaruHttpClientProvider(private val configurationRepository: ConfigurationRepository) :
    HttpClientProvider {
    private var currentHttpClient: HttpClient? = null

    override val httpClient = configurationRepository
        .configuration
        .map { configuration ->
            currentHttpClient?.close()

            val activeServerUrl = configuration.activeServerConfiguration?.url

            activeServerUrl?.let { serverUrl ->
                HttpClient {
                    install(Logging) {
                        level = io.ktor.client.plugins.logging.LogLevel.NONE
                    }

                    install(ContentNegotiation) {
                        json()
                    }

                    install(Resources)

                    defaultRequest {
                        url(serverUrl)
                    }
                }.also {
                    currentHttpClient = it
                }
            }
        }.stateIn(CoroutineScope(DispatcherProvider.io()), SharingStarted.Eagerly, null)
}

@Composable
fun ImmaruApp(platformSpecificModule: Module) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(platformSpecificModule, appModule)
        }
    ) {
        MainNavigation()
    }
}

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
