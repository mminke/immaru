package com.earthrevealed.immaru.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.route

fun Routing.api() {
    route("api") {
        collectionApi()
    }
}

