package com.earthrevealed.immaru.routes

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveStream
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingHandler
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.KtorDsl
import io.r2dbc.spi.ConnectionFactories
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
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

                assetRepository.getContentFor(asset).use { source ->
                    call.respondOutputStream(ContentType.parse(asset.mediaType.toString())) {
                        source.transferTo(this.asSink())
                    }

                }
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

                val contentType = call.request.contentType()
                if (!contentType.match(ContentType.Application.OctetStream)) {
                    call.respond(HttpStatusCode.BadRequest, "Unsupported media type")
                    return@put
                }

                call.receiveStream().asSource().buffered().use { source ->
                    assetRepository.saveContentFor(asset, source)
                }

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