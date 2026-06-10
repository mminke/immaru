package com.earthrevealed.immaru.routes.api

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetPage
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.assets.PageDirection
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
import kotlin.test.assertNotNull
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
        assertNotNull(assetRepository.findById(collectionId, fileAsset.id))
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
    fun `put content succeeds for large octet-stream payload`() = testApplication {
        val collectionId = CollectionId()
        val fileAsset = FileAsset(collectionId, "demo.jpg")
        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(fileAsset))
        val payload = checkNotNull(javaClass.classLoader.getResourceAsStream("large.png")) {
            "Missing test resource: large.png"
        }

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

    @Test
    fun `get paged assets returns not found when collection does not exist`() = testApplication {
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

        val response = client.get("/api/collections/$collectionId/assets?limit=20")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get paged assets returns first page ordered by original_created_at descending`() = testApplication {
        val collectionId = CollectionId()
        val oldest = FileAsset(collectionId, "oldest.jpg")
        val middle = FileAsset(collectionId, "middle.jpg")
        val newest = FileAsset(collectionId, "newest.jpg")
        oldest.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(1))
        middle.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(2))
        newest.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(3))

        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(oldest, middle, newest))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val response = client.get("/api/collections/$collectionId/assets?limit=2")

        assertEquals(HttpStatusCode.OK, response.status)

        val pageResponse = Json.decodeFromString<AssetPage>(response.body<String>())

        val assetIds = pageResponse.items.map { it.id }.toSet()
        assertEquals(2, assetIds.size)
        assertEquals(true, assetIds.containsAll(listOf(newest.id, middle.id)))
        assertEquals(true, pageResponse.hasMore)
        assertNotNull(pageResponse.nextCursor)
        assertEquals(middle.id, pageResponse.nextCursor!!.id)
    }

    @Test
    fun `get paged assets returns subsequent page using next cursor`() = testApplication {
        val collectionId = CollectionId()
        val oldest = FileAsset(collectionId, "oldest.jpg")
        val middle = FileAsset(collectionId, "middle.jpg")
        val newest = FileAsset(collectionId, "newest.jpg")
        oldest.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(1))
        middle.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(2))
        newest.registerContentDetails(MediaType.IMAGE_JPEG, byteArrayOf(3))

        val collectionRepository = InMemoryCollectionRepository(setOf(collectionId))
        val assetRepository = InMemoryAssetRepository(listOf(oldest, middle, newest))

        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                route("api") {
                    assetApi(collectionRepository, assetRepository)
                }
            }
        }

        val firstPage = Json.decodeFromString<AssetPage>(
            client.get("/api/collections/$collectionId/assets?limit=2").body<String>()
        )
        val nextCursor = firstPage.nextCursor!!

        val secondPage = Json.decodeFromString<AssetPage>(
            client.get(
                "/api/collections/$collectionId/assets" +
                        "?limit=2" +
                        "&cursorOriginalCreatedAt=${nextCursor.originalCreatedAt}" +
                        "&cursorId=${nextCursor.id}"
            ).body<String>()
        )

        assertEquals(1, secondPage.items.size)
        assertEquals(false, secondPage.hasMore)

        val secondIds = secondPage.items.map { it.id }.toSet()

        // second page should contain only the remaining oldest asset (order-agnostic)
        assertEquals(1, secondIds.size)
        assertEquals(true, secondIds.containsAll(listOf(oldest.id)))
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

@OptIn(ExperimentalUuidApi::class)
private class InMemoryAssetRepository(
    initialAssets: List<Asset> = emptyList(),
    initialContent: Map<AssetId, ByteArray> = emptyMap(),
) : AssetRepository {
    private val assets = initialAssets.associateBy { it.id }.toMutableMap()
    private val content = initialContent.toMutableMap()

    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? =
        assets[assetId]?.takeIf { it.collectionId == collectionId }

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> =
        assets.values.filter {
            it.collectionId == collectionId &&
                    (it as? FileAsset)?.status == AssetStatus.CONTENT_READY
        }

    override suspend fun save(asset: Asset) {
        assets[asset.id] = asset
    }

    override suspend fun delete(id: AssetId) {
        assets.remove(id)
        content.remove(id)
    }

    override suspend fun assetExists(assetId: AssetId): Boolean = assets.containsKey(assetId)

    override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> =
        flowOf(content[asset.id] ?: ByteArray(0))

    override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        val flattened = flatten(content)
        this.content[asset.id] = flattened

        if (asset.mediaTypeIsNotDefined) {
            asset.registerContentDetails(MediaType.IMAGE_JPEG, flattened)
        }
    }

    override suspend fun findSelectableDates(collectionId: CollectionId) =
        emptyList<com.earthrevealed.immaru.assets.SelectableYear>()

    override suspend fun findPageFor(
        collectionId: CollectionId,
        limit: Int,
        cursor: AssetCursor?,
        direction: PageDirection,
    ): AssetPage {
        // mirror R2DBC ordering: original_created_at DESC, id DESC (UUID string for tie-breaking)
        fun originalCreatedAt(a: Asset) = (a as? FileAsset)?.originalCreatedAt ?: a.auditFields.createdAt
        val descComparator = compareByDescending<Asset> { originalCreatedAt(it) }
            .thenByDescending { it.id.value.toString() }

        val allForCollection = assets.values.filter {
            it.collectionId == collectionId &&
                    (it as? FileAsset)?.status == AssetStatus.CONTENT_READY
        }

        val fetched = when {
            cursor == null ->
                allForCollection.sortedWith(descComparator)

            direction == PageDirection.FORWARD ->
                allForCollection.sortedWith(descComparator).filter { a ->
                    originalCreatedAt(a) < cursor.originalCreatedAt ||
                            (originalCreatedAt(a) == cursor.originalCreatedAt &&
                                    a.id.value.toString() < cursor.id.value.toString())
                }

            else -> // BACKWARD
                allForCollection.sortedWith(descComparator.reversed()).filter { a ->
                    originalCreatedAt(a) > cursor.originalCreatedAt ||
                            (originalCreatedAt(a) == cursor.originalCreatedAt &&
                                    a.id.value.toString() > cursor.id.value.toString())
                }
        }.take(limit + 1)

        val hasMore = fetched.size > limit
        val sliced = fetched.take(limit)
        val items = if (direction == PageDirection.BACKWARD && cursor != null) sliced.reversed() else sliced

        return AssetPage(
            items = items,
            nextCursor = items.lastOrNull()?.let { AssetCursor(originalCreatedAt(it), it.id) },
            prevCursor = items.firstOrNull()?.let { AssetCursor(originalCreatedAt(it), it.id) },
            hasMore = hasMore,
        )
    }

    fun storedContentFor(assetId: AssetId): ByteArray = content[assetId] ?: ByteArray(0)

    private suspend fun flatten(content: Flow<ByteArray>): ByteArray {
        val chunks = content.toList()
        val result = ByteArray(chunks.sumOf { it.size })
        var offset = 0
        chunks.forEach { chunk -> chunk.copyInto(result, offset); offset += chunk.size }
        return result
    }
}
