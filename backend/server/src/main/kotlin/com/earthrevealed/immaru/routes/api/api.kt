package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.collections.CollectionRepository
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject

fun Routing.api() {
    val collectionRepository by inject<CollectionRepository>()
    val assetRepository by inject<AssetRepository>()

    route("api") {
        collectionApi(collectionRepository, assetRepository)
        assetApi(collectionRepository, assetRepository)
    }
}

@KtorDsl
fun Route.get(
    vararg paths: String,
    body: RoutingHandler
) {
    paths.forEach { path ->
        get(path, body)
    }
}
