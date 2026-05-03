package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AssetApiTest {

    @Test
    fun `put assets returns not found when collection does not exist`() = testApplication {
        val collectionId = CollectionId()
        val collectionRepository = InMemoryCollectionRepository()
        val assetRepository = InMemoryAssetRepository()

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val payload = Json.encodeToString<Asset>(FileAsset(collectionId, "demo.jpg"))

        val response = client.put("/api/collections/$collectionId/assets") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `put assets returns bad request when payload collection differs from url`() = testApplication {
        val collectionInUrl = CollectionId()
        val payloadCollection = CollectionId()
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionInUrl, payloadCollection))
        val assetRepository = InMemoryAssetRepository()

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val payload = Json.encodeToString<Asset>(FileAsset(payloadCollection, "demo.jpg"))

        val response = client.put("/api/collections/$collectionInUrl/assets") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `put assets succeeds for valid payload`() = testApplication {
        val collectionId = CollectionId()
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository()

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val payload = Json.encodeToString<Asset>(fileAsset)

        val response = client.put("/api/collections/$collectionId/assets") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(1, assetRepository.findAllFor(collectionId).size)
    }

    @Test
    fun `put content returns unsupported media type when payload is not octet-stream`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.put("/api/collections/$collectionId/assets/${fileAsset.id}/content") {
            contentType(ContentType.Text.Plain)
            setBody("not-binary")
        }

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }

    @Test
    fun `put content returns not found when collection does not exist`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val collectionRepository = InMemoryCollectionRepository()
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.put("/api/collections/$collectionId/assets/${fileAsset.id}/content") {
            contentType(ContentType.Application.OctetStream)
            setBody(byteArrayOf(7, 8, 9))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `put content returns not found when asset does not exist`() = testApplication {
        val collectionId = CollectionId()
        val missingAssetId = AssetId()
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository()

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.put("/api/collections/$collectionId/assets/$missingAssetId/content") {
            contentType(ContentType.Application.OctetStream)
            setBody(byteArrayOf(7, 8, 9))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `put content succeeds for valid octet-stream payload`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset))
        val payload = byteArrayOf(1, 2, 3, 4, 5)

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.put("/api/collections/$collectionId/assets/${fileAsset.id}/content") {
            contentType(ContentType.Application.OctetStream)
            setBody(payload)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("File uploaded successfully", response.body<String>())
        assertContentEquals(payload, assetRepository.storedContentFor(fileAsset.id))
    }

    @Test
    fun `get content returns no content with no-cache header when media type is missing`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.get("/api/collections/$collectionId/assets/${fileAsset.id}/content")

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals("no-cache, no-store, must-revalidate", response.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun `get content returns binary payload for processed assets`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        fileAsset.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(1, 2, 3))

        val content = byteArrayOf(10, 20, 30, 40)
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset), mapOf(fileAsset.id to content))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.get("/api/collections/$collectionId/assets/${fileAsset.id}/content")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType]?.substringBefore(";"))
        assertContentEquals(content, response.body())
    }
}

private class InMemoryCollectionRepository(
    existingIds: Set<CollectionId> = emptySet(),
) : CollectionRepository {
    private val collections = mutableMapOf<CollectionId, Collection>()
    private val knownIds = existingIds.toMutableSet()

    override suspend fun save(collection: Collection) {
        collections[collection.id] = collection
        knownIds += collection.id
    }

    override suspend fun all(): List<Collection> = collections.values.toList()

    override suspend fun get(id: CollectionId): Collection? = collections[id]

    override suspend fun exists(id: CollectionId): Boolean = knownIds.contains(id)

    override suspend fun delete(id: CollectionId) {
        collections.remove(id)
        knownIds.remove(id)
    }
}

private class InMemoryAssetRepository(
    initialAssets: List<Asset> = emptyList(),
    initialContent: Map<AssetId, ByteArray> = emptyMap(),
) : AssetRepository {
    private val assets = initialAssets.associateBy { it.id }.toMutableMap()
    private val content = initialContent.toMutableMap()

    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? {
        return assets[assetId]?.takeIf { it.collectionId == collectionId }
    }

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return assets.values.filter { it.collectionId == collectionId }
    }

    override suspend fun save(asset: Asset) {
        assets[asset.id] = asset
    }

    override suspend fun delete(id: AssetId) {
        assets.remove(id)
        content.remove(id)
    }

    override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> {
        return flowOf(content[asset.id] ?: ByteArray(0))
    }

    override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        this.content[asset.id] = flatten(content)
    }

    override suspend fun findSelectableDates(collectionId: CollectionId) =
        emptyList<com.earthrevealed.immaru.assets.SelectableYear>()

    fun storedContentFor(assetId: AssetId): ByteArray = content[assetId] ?: ByteArray(0)

    private suspend fun flatten(content: Flow<ByteArray>): ByteArray {
        val chunks = content.toList()
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        chunks.forEach { chunk ->
            chunk.copyInto(result, destinationOffset = offset)
            offset += chunk.size
        }
        return result
    }
}
