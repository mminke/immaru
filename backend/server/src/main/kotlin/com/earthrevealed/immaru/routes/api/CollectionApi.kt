package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import com.earthrevealed.immaru.common.io.toFlow
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingHandler
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.KtorDsl
import io.ktor.utils.io.writeByteArray
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

fun Route.assetApi() {
    val connectionFactory = ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    val collectionRepository = R2dbcCollectionRepository(connectionFactory)
    val library = Library(Configuration.immaru.library.path)
    val assetRepository = R2dbcAssetRepository(connectionFactory, library)

    route("assets") {
        get {
            val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
            if (collectionRepository.get(collectionId) == null) { //TODO: replace with collectionRepository.exists(collectionId)
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                assetRepository.findAllFor(collectionId)
            )
        }

        put {
            val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
            val collection = collectionRepository.get(collectionId)
            if (collection == null) {
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

        route("/{asset-id}") {
            get {
                val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
                if (collectionRepository.get(collectionId) == null) { //TODO: replace with collectionRepository.exists(collectionId)
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val assetId = AssetId.fromString(call.parameters["asset-id"]!!)
                val asset = assetRepository.findById(collectionId, assetId)
                if (asset == null) {
                    logger.warn { "No asset found while trying to process content for asset." }
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(asset)
            }

            get("/content") {
                val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
                val collection = collectionRepository.get(collectionId)
                if (collection == null) {
                    logger.warn { "No collection found while trying to process content for asset." }
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val assetId = AssetId.fromString(call.parameters["asset-id"]!!)
                val asset = assetRepository.findById(collectionId, assetId)
                if (asset == null) {
                    logger.warn { "No asset found while trying to process content for asset." }
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                if (asset !is FileAsset) {
                    logger.warn { "Asset is not a file asset." }
                    call.respond(HttpStatusCode.BadRequest, "Not a file asset.")
                    return@get
                }

                logger.info { "Returning the contents of asset [asset.id=${asset.id}" }
                call.respondBytesWriter(
                    contentType = ContentType.parse(asset.mediaType.toString()),
                    producer = {
                        assetRepository.getContentFor(asset).collect {
                            this.writeByteArray(it)
                        }
                    },
                )
            }

            put("/content") {
                val collectionId = CollectionId.fromString(call.parameters["collection-id"]!!)
                val collection = collectionRepository.get(collectionId)
                if (collection == null) {
                    logger.warn { "No collection found while trying to process content for asset." }
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                val assetId = AssetId.fromString(call.parameters["asset-id"]!!)
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
    }
}

@KtorDsl
fun Route.get(
    vararg paths: String,
    body: RoutingHandler
) {
    paths.forEach { path ->
        get(path, body)
    }
}