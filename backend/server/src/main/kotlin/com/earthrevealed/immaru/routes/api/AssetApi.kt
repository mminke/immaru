package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import com.earthrevealed.immaru.common.io.toFlow
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.Route
import io.ktor.utils.io.writeByteArray
import io.r2dbc.spi.ConnectionFactories
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun Route.assetApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)
    val library = Library(Configuration.immaru.library.path)
    val assetRepository = R2dbcAssetRepository(connectionFactory, library)

    get<Collections.ById.Assets> { request ->
        if (collectionRepository.get(request.collection.id1) == null) { //TODO: replace with collectionRepository.exists(collectionId)
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(
            assetRepository.findAllFor(request.collection.id1)
        )
    }

    put<Collections.ById.Assets> { request ->
        val collection = collectionRepository.get(request.collection.id1)
        if (collection == null) {
            logger.warn { "No collection found while trying to create a new asset" }
            call.respond(HttpStatusCode.NotFound)
            return@put
        }

        val asset = call.receive<Asset>()
        if (asset.collectionId != request.collection.id1) {
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
        if (collectionRepository.get(collectionId) == null) { //TODO: replace with collectionRepository.exists(collectionId)
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
        val collection = collectionRepository.get(collectionId)
        if (collection == null) {
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
        val collectionId = request.asset.parent.collection.id1
        val collection = collectionRepository.get(collectionId)
        if (collection == null) {
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

        //TODO: Replace with something like expectContentType(..)
        val contentType = call.request.contentType()
        if (!contentType.match(ContentType.Application.OctetStream)) {
            call.respond(HttpStatusCode.BadRequest, "Unsupported media type")
            return@put
        }

        val content = call.receiveChannel().toFlow()
        assetRepository.saveContentFor(asset, content)

        call.respond(HttpStatusCode.OK, "File uploaded successfully")
    }
}