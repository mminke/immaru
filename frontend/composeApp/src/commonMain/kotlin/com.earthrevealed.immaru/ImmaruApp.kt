package com.earthrevealed.immaru

import Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earthrevealed.immaru.asset.AssetScreen
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionDetailsScreen
import com.earthrevealed.immaru.collections.CollectionScreen
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.KtorCollectionRepository
import com.earthrevealed.immaru.lightbox.LightboxScreen
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview


enum class Screen {
    Collections,
    NewCollection,
    CollectionDetails,
    Lightbox,
    Asset,
}

class GlobalViewModal {
    val currentCollection = mutableStateOf<Collection?>(null)

    private val _selectedAssets = MutableStateFlow<List<Asset>>(emptyList())
    val selectedAssets: StateFlow<List<Asset>> = _selectedAssets

    fun selectAssets(assets: List<Asset>) {
        _selectedAssets.value = assets
    }
}

@Composable
@Preview
fun ImmaruApp(
    navController: NavHostController = rememberNavController()
) {
    val globalViewModal = remember { GlobalViewModal() }
    val collectionRepository = KtorCollectionRepository(globalHttpClient)
    val assetRepository = KtorAssetRepository(globalHttpClient)

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = Screen.Collections.name,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(route = Screen.Collections.name) {
                CollectionScreen(
                    collectionRepository = collectionRepository,
                    onCollectionSelected = {
                        globalViewModal.currentCollection.value = it
                        navController.navigate(Screen.Lightbox.name)
                    },
                    onCollectionInfo = {
                        globalViewModal.currentCollection.value = it
                        navController.navigate(Screen.CollectionDetails.name)
                    },
                    onNewCollection = { navController.navigate(Screen.NewCollection.name) }
                )
            }
            composable(route = Screen.Lightbox.name) {
                LightboxScreen(
                    assetRepository,
                    globalViewModal.currentCollection.value!!,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) },
                    onViewAsset = { asset ->
                        globalViewModal.selectAssets(listOf(asset))
                        navController.navigate(Screen.Asset.name)
                    },
                    onAssetsSelected = { (assets) -> Unit },
                )
            }
            composable(route = Screen.CollectionDetails.name) {
                CollectionDetailsScreen(
                    collectionRepository,
                    globalViewModal.currentCollection.value!!,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.NewCollection.name) {
                CollectionDetailsScreen(
                    collectionRepository,
                    collection { },
                    isNew = true,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.Asset.name) {
                AssetScreen(
                    globalViewModal.selectedAssets.collectAsState().value.first(),
                    onNavigateBack = { navController.navigate(Screen.Lightbox.name) }
                )
            }
        }
    }
}

val globalHttpClient = try {
    HttpClient {
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            url(Configuration.immaruUrl)
        }
    }
} catch (exception: RuntimeException) {
    throw RuntimeException("globalHttpClient initialization error.")
}
