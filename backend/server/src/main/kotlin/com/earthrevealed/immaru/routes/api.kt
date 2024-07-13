package com.earthrevealed.immaru.routes

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.flowOf

fun Routing.api() {
    route("api") {
        collectionResource()
    }
}

fun Route.collectionResource() {
    route("collections") {
        get {
            call.respond(
                flowOf(
                    Collection(CollectionId(), "first collection", "2023"),
                    Collection(CollectionId(), "second collection", "2024"),
                )
            )
        }
        get("/") {
            call.respond(
                flowOf(
                    Collection(CollectionId(), "first collection", "2023"),
                    Collection(CollectionId(), "second collection", "2024"),
                )
            )
        }
    }
}
