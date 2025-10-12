package com.earthrevealed.immaru.routes.api

import io.ktor.server.routing.*
import io.ktor.utils.io.*

fun Routing.api() {
    route("api") {
        collectionApi()
        assetApi()

        infoApi()
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