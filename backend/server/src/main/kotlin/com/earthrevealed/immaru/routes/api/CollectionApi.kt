package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.resources.resource
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.r2dbc.spi.ConnectionFactories
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@Resource("collections")
class CollectionsResource {
    @Resource("{id}")
    class ById(val parent: CollectionsResource = CollectionsResource(), val id: CollectionId)
}

fun Route.collectionApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)


    get<CollectionsResource> {
        call.respond(
            collectionRepository.all()
        )
    }
    put<CollectionsResource> {
        val collection = call.receive<Collection>()
        collectionRepository.save(collection)
        call.response.status(HttpStatusCode.NoContent)
    }
    get<CollectionsResource.ById> { request ->
        collectionRepository.get(request.id)?.apply {
            call.respond(this)
        }
    }
    delete<CollectionsResource.ById> { request ->
        collectionRepository.delete(request.id)
        call.response.status(HttpStatusCode.NoContent)
    }

    resource<CollectionsResource> {
        route("/{collection-id}") {
            assetApi()

            selectorsApi()
        }
    }
}
