package com.earthrevealed.immaru.routes

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Routing

fun Routing.info() {
    staticResources("", "public")
}