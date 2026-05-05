package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get

fun Route.selectorsApi(
    collectionRepository: CollectionRepository,
    assetRepository: AssetRepository,
) {
    route("available-date-selectors") {
        get {
            val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
            if (!collectionRepository.exists(collectionId)) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                assetRepository.findSelectableDates(collectionId)
            )
        }
    }
}
