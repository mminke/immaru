package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.maintenance.MaintenanceService
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingHandler
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.utils.io.KtorDsl
import org.koin.ktor.ext.inject

fun Routing.api() {
    val collectionRepository by inject<CollectionRepository>()
    val assetRepository by inject<AssetRepository>()
    val maintenanceService by inject<MaintenanceService>()

    route("api") {
        collectionApi(collectionRepository, assetRepository)
        assetApi(collectionRepository, assetRepository)
        maintenanceApi(maintenanceService, collectionRepository)
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
