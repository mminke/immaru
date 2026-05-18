package com.earthrevealed.immaru

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earthrevealed.immaru.asset.AssetDetailsViewModel
import com.earthrevealed.immaru.asset.AssetScreen
import com.earthrevealed.immaru.asset.AssetViewModel
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionDetailsScreen
import com.earthrevealed.immaru.collections.CollectionDetailsViewModel
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.CollectionsScreen
import com.earthrevealed.immaru.collections.CollectionsViewModel
import com.earthrevealed.immaru.collections.collection
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module


enum class Screen {
    Configuration,
    Collections,
    NewCollection,
    CollectionDetails,
    Lightbox,
    Asset,
}

private object Routes {
    const val Configuration = "configuration"
    const val Collections = "collections"
    const val NewCollection = "new_collection"
    const val CollectionDetails = "collection_details"
    object Asset {
        const val ARG_COLLECTION_ID = "collectionId"
        const val ARG_ASSET_ID = "assetId"
        const val pattern = "asset/{$ARG_COLLECTION_ID}/{$ARG_ASSET_ID}"

        fun create(collectionId: String, assetId: String) = "asset/$collectionId/$assetId"
    }

    object Lightbox {
        const val ARG_COLLECTION_ID = "collectionId"
        const val pattern = "lightbox/{$ARG_COLLECTION_ID}"

        fun create(collectionId: String) = "lightbox/$collectionId"
    }
}

class GlobalViewModel(
    configurationRepository: ConfigurationRepository
) : ViewModel() {
    val configuration = configurationRepository.configuration

    val currentCollection = mutableStateOf<Collection?>(null)
    val currentAsset = mutableStateOf<Asset?>(null)
}

val appModule = module {
    singleOf(::DataStoreConfigurationRepository) { bind<ConfigurationRepository>() }

    singleOf(::ImmaruHttpClientProvider) { bind<HttpClientProvider>() }

    singleOf(::KtorCollectionRepository) { bind<CollectionRepository>() }
    singleOf(::KtorAssetRepository) { bind<AssetRepository>() }

    viewModelOf(::ConfigurationViewModel)
    viewModelOf(::GlobalViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModel {
        CollectionDetailsViewModel(get(), get(), get())
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
    globalViewModel: GlobalViewModel = koinViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val httpClientProvider = koinInject<HttpClientProvider>()
    val httpClient = httpClientProvider.httpClient.collectAsState()

    val startDestination = if (httpClient.value == null) {
        Routes.Configuration
    } else {
        Routes.Collections
    }

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(route = Routes.Configuration) {
                val configuration = globalViewModel.configuration.collectAsState(Configuration())

                ConfigurationScreen(
                    configuration.value,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(route = Routes.Collections) {
                CollectionsScreen(
                    onCollectionSelected = {
                        globalViewModel.currentCollection.value = it
                        navController.navigate(Routes.Lightbox.create(it.id.toString()))
                    },
                    onCollectionInfo = {
                        globalViewModel.currentCollection.value = it
                        navController.navigate(Routes.CollectionDetails)
                    },
                    onNewCollection = { navController.navigate(Routes.NewCollection) },
                    onOpenConfiguration = { navController.navigate(Routes.Configuration) }
                )
            }
            composable(route = Routes.Lightbox.pattern) {
                val currentCollection = globalViewModel.currentCollection.value
                if (currentCollection == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                LightboxScreen(
                    currentCollection.id,
                    onNavigateBack = { navController.popBackStack() },
                    onViewAsset = { asset ->
                        globalViewModel.currentAsset.value = asset
                        navController.navigate(
                            Routes.Asset.create(
                                asset.collectionId.value.toString(),
                                asset.id.value.toString(),
                            )
                        )
                    },
                    onAssetsSelected = { (assets) -> Unit },
                )
            }
            composable(route = Routes.CollectionDetails) {
                val currentCollection = globalViewModel.currentCollection.value
                if (currentCollection == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                CollectionDetailsScreen(
                    currentCollection,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Routes.NewCollection) {
                CollectionDetailsScreen(
                    collection { },
                    isNew = true,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Routes.Asset.pattern) {
                val currentAsset = globalViewModel.currentAsset.value
                if (currentAsset == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                AssetScreen(
                    collectionId = currentAsset.collectionId,
                    assetId = currentAsset.id,
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
