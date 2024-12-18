package com.earthrevealed.immaru

import Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionDetailsScreen
import com.earthrevealed.immaru.collections.CollectionDetailsViewModel
import com.earthrevealed.immaru.collections.CollectionScreen
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.KtorCollectionRepository
import com.earthrevealed.immaru.lightbox.LightboxScreen
import com.earthrevealed.immaru.lightbox.LightboxViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import org.jetbrains.compose.ui.tooling.preview.Preview


enum class Screen {
    Collections,
    Lightbox,
    NewCollection,
    CollectionDetails
}

@Composable
@Preview
fun ImmaruApp(
    navController: NavHostController = rememberNavController()
) {
    val collectionRepository = KtorCollectionRepository(globalHttpClient)
    val assetRepository = KtorAssetRepository(globalHttpClient)

    val currentCollection = mutableStateOf<Collection?>(null)

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
                        currentCollection.value = it
                        navController.navigate(Screen.Lightbox.name)
                    },
                    onCollectionInfo = {
                        currentCollection.value = it
                        navController.navigate(Screen.CollectionDetails.name)
                    },
                    onNewCollection = { navController.navigate(Screen.NewCollection.name) }
                )
            }
            composable(route = Screen.Lightbox.name) {
                LightboxScreen(
                    assetRepository,
                    currentCollection.value!!,
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.CollectionDetails.name) {
                CollectionDetailsScreen(
                    viewModel = CollectionDetailsViewModel(
                        collectionRepository,
                        currentCollection.value!!
                    ),
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
                )
            }
            composable(route = Screen.NewCollection.name) {
                CollectionDetailsScreen(
                    viewModel = CollectionDetailsViewModel(
                        collectionRepository,
                        collection { },
                        isNew = true
                    ),
                    onNavigateBack = { navController.navigate(Screen.Collections.name) }
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
