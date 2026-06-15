package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.maintenance.MaintenanceService
import com.earthrevealed.immaru.maintenance.api.Maintenance
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun Route.maintenanceApi(
    maintenanceService: MaintenanceService,
    collectionRepository: CollectionRepository,
) {
    put<Maintenance.OrphanedAssets> { request ->
        val collectionId = request.collectionId

        if (!collectionRepository.exists(collectionId)) {
            logger.warn { "Invalid collection id while trying to create assets for orphaned files. [CollectionId=$collectionId]" }
            call.respond(HttpStatusCode.BadRequest, "Invalid collection id")
            return@put
        }

        // Launch async processing
        call.application.launch {
            try {
                maintenanceService.createAssetsForOrphanedFiles(collectionId).collect { assetId ->
                    logger.info { "Created asset for orphaned file: $assetId" }
                }
                logger.info { "Completed creating assets for orphaned files in collection: $collectionId" }
            } catch (e: Exception) {
                logger.error(e) { "Error creating assets for orphaned files in collection: $collectionId" }
            }
        }

        call.respond(HttpStatusCode.Accepted, mapOf("message" to "Processing orphaned files in background"))
    }
}
