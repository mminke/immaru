package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.BuildInfo
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.infoApi() {
    route("info") {
        get("", "/") {
            call.respond(
                BuildInfo()
            )
            //obtain
            // environment
            // last commit message
            // last commit timestamp
        }
    }
}