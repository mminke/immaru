package com.earthrevealed.immaru.assets.repositories.r2dbc

import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType.Companion.IMAGE_JPEG
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.library.useResourceAsFlow
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.support.FixedClock
import com.earthrevealed.immaru.support.truncateNanos
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

val PostgresqlContainer = PostgreSQLContainer("postgres:17.2")

val <PostgreSQLContainer> PostgreSQLContainer.r2dbcUrl: String
    get() {
        val host = PostgresqlContainer.host
        val port = PostgresqlContainer.getMappedPort(POSTGRESQL_PORT)
        val databaseName = PostgresqlContainer.databaseName
        val username = PostgresqlContainer.username
        val password = PostgresqlContainer.password
        return "r2dbc:postgresql://$username:$password@$host:$port/$databaseName"
    }

class R2dbcAssetRepositoryIT {
    private lateinit var collectionRepository: CollectionRepository
    private val library = Library(Path(SystemTemporaryDirectory, "immaru-testing"))
    private lateinit var assetRepository: AssetRepository
    private val collection = collection { name = "Test collection" }

    @BeforeTest
    fun setup() {
        PostgresqlContainer.start()

        Flyway.configure()
            .dataSource(
                PostgresqlContainer.jdbcUrl,
                PostgresqlContainer.username,
                PostgresqlContainer.password
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()

        val connectionFactory =
            ConnectionFactories.get(PostgresqlContainer.r2dbcUrl.also { println("R2DBC url: $it") })
        collectionRepository = R2dbcCollectionRepository(connectionFactory)
        assetRepository = R2dbcAssetRepository(connectionFactory, library)

        runBlocking {
            collectionRepository.save(collection)
        }
    }

    @AfterTest
    fun tearDown() {
        PostgresqlContainer.stop()
    }

    @Test
    fun `test saving a new file asset and retrieving it`() {
        val fileAsset = FileAsset(
            collection.id,
            "test.jpg",
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val results = assetRepository.findAllFor(collection.id)
            assertEquals(0, results.size)

            fileAsset.registerContentDetails(IMAGE_JPEG, ByteArray(0))
            assetRepository.save(fileAsset)

            val readyResults = assetRepository.findAllFor(collection.id)
            assertEquals(1, readyResults.size)
            assertEquals(fileAsset, readyResults[0])

            val result = assetRepository.findById(collection.id, fileAsset.id)
            assertNotNull(result)
            assertEquals(fileAsset, result)
        }
    }

    @Test
    fun `test saving a new file asset without media-type and retrieving it`() {
        val fileAsset = FileAsset(
            collectionId = collection.id,
            originalFilename = "1234.jpg",
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val result = assetRepository.findById(collection.id, fileAsset.id)
            assertNotNull(result)
            assertEquals(fileAsset, result)
        }
    }

    @Test
    fun `test saving a new file asset and updating it`() {
        val fileAsset = FileAsset(
            collectionId = collection.id,
            originalFilename = "1234.jpg",
        )

        runBlocking {
            assetRepository.save(fileAsset)

            val assetToModify = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset
            assertEquals("1234.jpg", assetToModify.name)
            assertNull(assetToModify.mediaType)
            assertEquals(
                fileAsset.auditFields.lastModifiedAt.truncateNanos(),
                assetToModify.auditFields.createdAt.truncateNanos()
            )

            delay(1000.milliseconds)

            assetToModify.changeName("my new name")
            assetToModify.registerContentDetails(IMAGE_JPEG, ByteArray(0))

            assetRepository.save(assetToModify)
            val result = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset

            assertNotNull(result)
            assertEquals("my new name", result.name)
            assertEquals(IMAGE_JPEG, result.mediaType)
            assertNotEquals(
                result.auditFields.lastModifiedAt.truncateNanos(),
                result.auditFields.createdAt.truncateNanos()
            )
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test processing contents of a jpg file`() {
        useResourceAsFlow("large.jpg") { jpgContent ->
            val fileAsset = FileAsset(
                collectionId = collection.id,
                originalFilename = "large.jpg",
            )

            runBlocking {
                assetRepository.save(fileAsset)
                assetRepository.saveContentFor(fileAsset, jpgContent)

                val result = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset

                assertEquals(IMAGE_JPEG, result.mediaType)
                assertEquals(AssetStatus.CONTENT_READY, result.status)
                assertEquals(
                    "9a44941679e7111fe3d35409017ba488a8f592b8a8692c91926d3e914a3ada23",
                    result.contentHash!!.toHexString()
                )
            }
        }
    }

    @Test
    fun `test findPageFor supports forward paging`() {
        runBlocking {
            val createdAssets = withFixedClock {
                createAssetsAt(
                    instants = listOf(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:01Z"),
                        Instant.parse("2025-01-01T00:00:02Z"),
                        Instant.parse("2025-01-01T00:00:03Z"),
                        Instant.parse("2025-01-01T00:00:04Z"),
                    )
                )
            }

            val firstPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = null,
                direction = PageDirection.FORWARD,
            )

            assertEquals(listOf(createdAssets[4].id, createdAssets[3].id), firstPage.items.map { it.id })
            assertTrue(firstPage.hasMore)
            assertNotNull(firstPage.nextCursor)

            val secondPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = firstPage.nextCursor,
                direction = PageDirection.FORWARD,
            )

            assertEquals(listOf(createdAssets[2].id, createdAssets[1].id), secondPage.items.map { it.id })
            assertTrue(secondPage.hasMore)
            assertNotNull(secondPage.nextCursor)

            val thirdPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = secondPage.nextCursor,
                direction = PageDirection.FORWARD,
            )

            assertEquals(listOf(createdAssets[0].id), thirdPage.items.map { it.id })
            assertFalse(thirdPage.hasMore)
        }
    }

    @Test
    fun `test findPageFor supports backward paging from a middle page`() {
        runBlocking {
            val createdAssets = withFixedClock {
                createAssetsAt(
                    instants = listOf(
                        Instant.parse("2025-02-01T00:00:00Z"),
                        Instant.parse("2025-02-01T00:00:01Z"),
                        Instant.parse("2025-02-01T00:00:02Z"),
                        Instant.parse("2025-02-01T00:00:03Z"),
                        Instant.parse("2025-02-01T00:00:04Z"),
                    )
                )
            }

            val middlePage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = null,
                direction = PageDirection.FORWARD,
            ).let {
                assetRepository.findPageFor(
                    collectionId = collection.id,
                    limit = 2,
                    cursor = it.nextCursor,
                    direction = PageDirection.FORWARD,
                )
            }

            val previousPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = middlePage.prevCursor,
                direction = PageDirection.BACKWARD,
            )

            assertEquals(listOf(createdAssets[4].id, createdAssets[3].id), previousPage.items.map { it.id })
            assertFalse(previousPage.hasMore)
        }
    }

    @Test
    fun `test findPageFor does not duplicate items when multiple assets share timestamp`() {
        runBlocking {
            val fixedInstant = Instant.parse("2025-03-01T00:00:00Z")
            val createdAssets = withFixedClock {
                createAssetsAt(instants = List(4) { fixedInstant })
            }

            val firstPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = null,
                direction = PageDirection.FORWARD,
            )

            val secondPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = firstPage.nextCursor,
                direction = PageDirection.FORWARD,
            )

            val allPagedIds = (firstPage.items + secondPage.items).map { it.id }
            assertEquals(4, allPagedIds.size)
            assertEquals(4, allPagedIds.distinct().size)
            assertEquals(createdAssets.map { it.id }.toSet(), allPagedIds.toSet())
            assertFalse(secondPage.hasMore)
        }
    }

    @Test
    fun `test findPageFor returns empty page when cursor is exhausted`() {
        runBlocking {
            withFixedClock {
                createAssetsAt(
                    instants = listOf(
                        Instant.parse("2025-04-01T00:00:00Z"),
                        Instant.parse("2025-04-01T00:00:01Z"),
                        Instant.parse("2025-04-01T00:00:02Z"),
                    )
                )
            }

            val firstPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = null,
                direction = PageDirection.FORWARD,
            )

            val lastNonEmptyPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = firstPage.nextCursor,
                direction = PageDirection.FORWARD,
            )

            assertFalse(lastNonEmptyPage.hasMore)
            assertNotNull(lastNonEmptyPage.nextCursor)

            val exhaustedPage = assetRepository.findPageFor(
                collectionId = collection.id,
                limit = 2,
                cursor = lastNonEmptyPage.nextCursor,
                direction = PageDirection.FORWARD,
            )

            assertTrue(exhaustedPage.items.isEmpty())
            assertNull(exhaustedPage.nextCursor)
            assertNull(exhaustedPage.prevCursor)
            assertFalse(exhaustedPage.hasMore)
        }
    }

    private suspend fun createAssetsAt(instants: List<Instant>): List<FileAsset> {
        return instants.mapIndexed { index, instant ->
            FixedClock.setClockTo(instant)
            FileAsset(
                collectionId = collection.id,
                originalFilename = "asset-$index.jpg",
            ).also {
                it.registerContentDetails(IMAGE_JPEG, byteArrayOf(index.toByte()))
                assetRepository.save(it)
            }
        }
    }

    private suspend fun <T> withFixedClock(block: suspend () -> T): T {
        ClockProvider.clock = FixedClock
        FixedClock.reset()

        try {
            return block()
        } finally {
            ClockProvider.clock = Clock.System
        }
    }
}
