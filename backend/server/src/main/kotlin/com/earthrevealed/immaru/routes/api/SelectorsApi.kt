package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.r2dbc.spi.ConnectionFactories

fun Route.selectorsApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)
    val library = Library(Configuration.immaru.library.path)
    val assetRepository = R2dbcAssetRepository(connectionFactory, library)

    route("available-date-selectors") {
        get {
            val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
            if (collectionRepository.get(collectionId) == null) { //TODO: replace with collectionRepository.exists(collectionId)
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                assetRepository.findSelectableDates(collectionId)
            )
        }
    }
}