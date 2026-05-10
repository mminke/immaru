package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.assets.api.Collections
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.common.io.toFlow
import com.earthrevealed.ktor.extensions.common.expectContentType
import io.ktor.http.*
import io.ktor.http.ContentType.Application.OctetStream
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.utils.io.*
import mu.KotlinLogging
import kotlin.time.Instant

private val logger = KotlinLogging.logger { }

fun Route.assetApi(
    collectionRepository: CollectionRepository,
    assetRepository: AssetRepository,
) {
    get<Collections.ById.Assets> { request ->
        val collectionId = request.collection.id1
        if (!collectionRepository.exists(collectionId)) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val paginationRequested =
            request.limit != null ||
                    request.cursorCreatedAt != null ||
                    request.cursorId != null ||
                    request.direction.uppercase() != PageDirection.FORWARD.name

        if (!paginationRequested) {
            call.respond(assetRepository.findAllFor(collectionId))
            return@get
        }

        val limit = request.limit
        if (limit == null) {
            call.respond(HttpStatusCode.BadRequest, "limit query parameter is required for pagination")
            return@get
        }

        val direction = runCatching { PageDirection.valueOf(request.direction.uppercase()) }
            .getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Unsupported direction '${request.direction}'")
                return@get
            }

        val cursor = when {
            request.cursorCreatedAt == null && request.cursorId == null -> null
            request.cursorCreatedAt != null && request.cursorId != null -> {
                val createdAt = runCatching { Instant.parse(request.cursorCreatedAt!!) }
                    .getOrElse {
                        call.respond(HttpStatusCode.BadRequest, "Invalid cursorCreatedAt")
                        return@get
                    }

                val assetId = runCatching { AssetId.fromString(request.cursorId!!) }
                    .getOrElse {
                        call.respond(HttpStatusCode.BadRequest, "Invalid cursorId")
                        return@get
                    }

                AssetCursor(createdAt, assetId)
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest, "Both cursorCreatedAt and cursorId must be provided")
                return@get
            }
        }

        call.respond(assetRepository.findPageFor(collectionId, limit, cursor, direction))
    }

    put<Collections.ById.Assets> { request ->
        val collectionId = request.collection.id1
        if (!collectionRepository.exists(collectionId)) {
            logger.warn { "No collection found while trying to create a new asset" }
            call.respond(HttpStatusCode.NotFound)
            return@put
        }

        val asset = call.receive<Asset>()
        if (asset.collectionId != collectionId) {
            logger.warn { "Collection id in asset is not the same as the collection specified in url" }
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        assetRepository.save(asset)
        call.response.status(HttpStatusCode.NoContent)
    }

    get<Collections.ById.Assets.ById> { request ->
        val collectionId = request.parent.collection.id1
        val assetId = request.id2
        if (!collectionRepository.exists(collectionId)) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val asset = assetRepository.findById(collectionId, assetId)
        if (asset == null) {
            logger.warn { "No asset found while trying to get asset. [AssetId=$assetId,CollectionId=$collectionId]" }
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(asset)
    }

    get<Collections.ById.Assets.ById.Content> { request ->
        val collectionId = request.asset.parent.collection.id1
        if (!collectionRepository.exists(collectionId)) {
            logger.warn { "No collection found while trying to process content for asset." }
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val assetId = request.asset.id2
        val asset = assetRepository.findById(collectionId, assetId)
        if (asset == null) {
            logger.warn { "No asset found while trying to process content for asset. [AssetId=$assetId,CollectionId=$collectionId]" }
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        if (asset !is FileAsset) {
            logger.warn { "Asset is not a file asset  [AssetId=$assetId,CollectionId=$collectionId]" }
            call.respond(HttpStatusCode.BadRequest, "Not a file asset.")
            return@get
        }
        if (asset.mediaType == null) {
            logger.warn { "Asset content is not processed yet. [AssetId=$assetId,CollectionId=$collectionId]" }
            call.response.header("Cache-Control", "no-cache, no-store, must-revalidate")
            call.respond(HttpStatusCode.NoContent, "Content not processed yet.")
            return@get
        }

        logger.info { "Returning the contents of asset [AssetId=$assetId,CollectionId=$collectionId]" }
        call.respondBytesWriter(
            contentType = ContentType.parse(asset.mediaType.toString()),
            producer = {
                assetRepository.getContentFor(asset).collect {
                    this.writeByteArray(it)
                }
            },
        )
    }

    put<Collections.ById.Assets.ById.Content> { request ->
        expectContentType(OctetStream) { return@put }

        val collectionId = request.asset.parent.collection.id1
        if (!collectionRepository.exists(collectionId)) {
            logger.warn { "No collection found while trying to process content for asset." }
            call.respond(HttpStatusCode.NotFound)
            return@put
        }

        val assetId = request.asset.id2
        val asset = assetRepository.findById(collectionId, assetId)
        if (asset == null) {
            logger.warn { "No asset found while trying to process content for asset." }
            call.respond(HttpStatusCode.NotFound)
            return@put
        }
        if (asset !is FileAsset) {
            logger.warn { "Asset is not a file asset." }
            call.respond(HttpStatusCode.BadRequest, "Not a file asset.")
            return@put
        }

        val content = call.receiveChannel().toFlow()
        assetRepository.saveContentFor(asset, content)

        call.respond(HttpStatusCode.OK, "File uploaded successfully")
    }

}
