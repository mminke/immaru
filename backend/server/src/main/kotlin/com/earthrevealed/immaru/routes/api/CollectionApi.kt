package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.r2dbc.spi.ConnectionFactories
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun Route.collectionApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)

    get<Collections> {
        call.respond(
            collectionRepository.all()
        )
    }
    put<Collections> {
        val collection = call.receive<Collection>()
        collectionRepository.save(collection)
        call.response.status(HttpStatusCode.NoContent)
    }
    get<Collections.ById> { request ->
        collectionRepository.get(request.id1)?.apply {
            call.respond(this)
        }
    }
    delete<Collections.ById> { request ->
        collectionRepository.delete(request.id1)
        call.response.status(HttpStatusCode.NoContent)
    }

    resource<Collections> {
        route("/{collection-id}") {

            selectorsApi()
        }
    }
}
