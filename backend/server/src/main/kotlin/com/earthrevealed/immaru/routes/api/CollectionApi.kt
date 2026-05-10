package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.api.Collections
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.collectionApi(
    collectionRepository: CollectionRepository,
    assetRepository: AssetRepository,
) {
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
            selectorsApi(collectionRepository, assetRepository)
        }
    }
}
