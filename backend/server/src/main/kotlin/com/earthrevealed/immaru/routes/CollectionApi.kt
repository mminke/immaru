package com.earthrevealed.immaru.routes

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.r2dbc.spi.ConnectionFactories

fun Route.collectionApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)

    route("collections") {
        get {
            call.respond(
                collectionRepository.all()
            )
        }
        get("/") {
            call.respond(
                collectionRepository.all()
            )
        }
        put {
            val collection = call.receive<Collection>()
            collectionRepository.save(collection)
            call.response.status(HttpStatusCode.NoContent)
        }
        route("/{id}") {
            delete {
                val collectionId = CollectionId.fromString(call.parameters["id"]!!)
                collectionRepository.delete(collectionId)
                call.response.status(HttpStatusCode.NoContent)
            }

            assetApi()
        }
    }
}

fun Route.assetApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val assetRepository = R2dbcAssetRepository(connectionFactory)

    route("assets") {
        get {
            val collectionId = CollectionId.fromString(call.parameters["id"]!!)
            call.respond(
                assetRepository.findAllFor(collectionId)
            )
        }

    }
}