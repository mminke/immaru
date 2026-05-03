package com.earthrevealed.ktor.extensions.buildinfo

import com.earthrevealed.immaru.routes.api.get
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route

fun Routing.buildInfo() {
    staticResources("", "public")

    route("api") {
        route("build-info") {
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
}
