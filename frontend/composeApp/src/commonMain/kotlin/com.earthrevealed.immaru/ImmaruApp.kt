package com.earthrevealed.immaru

import Configuration
import DataStoreConfigurationRepository
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earthrevealed.immaru.asset.AssetScreen
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionDetailsScreen
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.CollectionScreen
import com.earthrevealed.immaru.collections.CollectionsViewModel
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.KtorCollectionRepository
import com.earthrevealed.immaru.common.HttpClientProvider
import com.earthrevealed.immaru.configuration.ConfigurationScreen
import com.earthrevealed.immaru.configuration.ConfigurationViewModel
import com.earthrevealed.immaru.lightbox.LightboxScreen
import com.earthrevealed.immaru.lightbox.LightboxViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


enum class Screen {
    Configuration,
    Collections,
    NewCollection,
    CollectionDetails,
    Lightbox,
    Asset,
}

class GlobalViewModel : ViewModel() {
    val currentCollection = mutableStateOf<Collection?>(null)

    private val _selectedAssets = MutableStateFlow<List<Asset>>(emptyList())
    val selectedAssets: StateFlow<List<Asset>> = _selectedAssets

    fun selectAssets(assets: List<Asset>) {
        _selectedAssets.value = assets
    }
}

val appModule = module {
    singleOf(::DataStoreConfigurationRepository)

    single {
        val dataStore: DataStore<Preferences> = get()

        HttpClient {
            install(ContentNegotiation) {
                json()
            }

            defaultRequest {
                url(Configuration.immaruUrl)
            }
        }
    }

    singleOf(::ImmaruHttpClientProvider) { bind<HttpClientProvider>() }

    singleOf(::KtorCollectionRepository) { bind<CollectionRepository>() }
    singleOf(::KtorAssetRepository) { bind<AssetRepository>() }

    viewModelOf(::ConfigurationViewModel)
    viewModelOf(::GlobalViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::LightboxViewModel)
}

class ImmaruHttpClientProvider(private val configurationRepository: DataStoreConfigurationRepository) :
    HttpClientProvider {
    private var currentHttpClient: HttpClient? = null

    override val httpClient = configurationRepository.getValue("immaru.server.url")
        .map { serverUrl ->
            currentHttpClient?.close()

            serverUrl?.let {
                HttpClient {
                    install(ContentNegotiation) {
                        json()
                    }

                    defaultRequest {
                        url(serverUrl)
                    }
                }.also {
                    currentHttpClient = it
                }
            }
        }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
}

@Composable
fun ImmaruApp(platformSpecificModule: Module) {
    KoinApplication(application = {
        platformSpecificModule.apply { modules(this) }
        modules(appModule)
    }) {
        val httpClientProvider = koinInject<HttpClientProvider>()
        val httpClient = httpClientProvider.httpClient.collectAsState()

        val configurationRepository = koinInject<DataStoreConfigurationRepository>()
        val serverUrl: State<String> = configurationRepository.getValue("immaru.server.url")
            .filterNotNull()
            .collectAsState("")

        if (httpClient.value == null) {
            ConfigurationScreen(serverUrl.value)
        } else {
            MainNavigation()
        }
    }
}

@Composable
@Preview
fun MainNavigation(
    globalViewModel: GlobalViewModel = koinViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = Screen.Collections.name,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(route = Screen.Configuration.name) {
                ConfigurationScreen("todo")
            }
            composable(route = Screen.Collections.name) {
                CollectionScreen(
                    onCollectionSelected = {
                        globalViewModel.currentCollection.value = it
                        navController.navigate(Screen.Lightbox.name)
                    },
                    onCollectionInfo = {
                        globalViewModel.currentCollection.value = it
                        navController.navigate(Screen.CollectionDetails.name)
                    },
                    onNewCollection = { navController.navigate(Screen.NewCollection.name) }
                )
            }
            composable(route = Screen.Lightbox.name) {
                LightboxScreen(
                    globalViewModel.currentCollection.value!!,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) },
                    onViewAsset = { asset ->
                        globalViewModel.selectAssets(listOf(asset))
                        navController.navigate(Screen.Asset.name)
                    },
                    onAssetsSelected = { (assets) -> Unit },
                )
            }
            composable(route = Screen.CollectionDetails.name) {
                CollectionDetailsScreen(
                    globalViewModel.currentCollection.value!!,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.NewCollection.name) {
                CollectionDetailsScreen(
                    collection { },
                    isNew = true,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.Asset.name) {
                AssetScreen(
                    globalViewModel.selectedAssets.collectAsState().value.first(),
                    onNavigateBack = { navController.navigate(Screen.Lightbox.name) }
                )
            }
        }
    }
}
