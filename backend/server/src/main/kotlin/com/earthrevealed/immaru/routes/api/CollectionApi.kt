package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.r2dbc.spi.ConnectionFactories
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun Route.collectionApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)

    route("collections") {
        get("", "/") {
            call.respond(
                collectionRepository.all()
            )
        }
        put {
            val collection = call.receive<Collection>()
            collectionRepository.save(collection)
            call.response.status(HttpStatusCode.NoContent)
        }
        route("/{collection-id}") {
            delete {
                val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
                collectionRepository.delete(collectionId)
                call.response.status(HttpStatusCode.NoContent)
            }

            assetApi()

            selectorsApi()
        }
    }
}
