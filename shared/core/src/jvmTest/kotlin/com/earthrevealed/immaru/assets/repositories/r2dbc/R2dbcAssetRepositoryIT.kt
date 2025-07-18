package com.earthrevealed.immaru.assets.repositories.r2dbc

import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType.Companion.IMAGE_JPEG
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.test.utils.useResourceAsFlow
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.collection
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
            assertEquals(1, results.size)
            assertEquals(fileAsset, results[0])

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
                fileAsset.auditFields.lastModifiedOn.truncateNanos(),
                assetToModify.auditFields.createdOn.truncateNanos()
            )

            delay(1000)

            assetToModify.changeName("my new name")
            assetToModify.registerContentDetails(IMAGE_JPEG, ByteArray(0))

            assetRepository.save(assetToModify)
            val result = assetRepository.findById(collection.id, fileAsset.id)!! as FileAsset

            assertNotNull(result)
            assertEquals("my new name", result.name)
            assertEquals(IMAGE_JPEG, result.mediaType)
            assertNotEquals(
                result.auditFields.lastModifiedOn.truncateNanos(),
                result.auditFields.createdOn.truncateNanos()
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
                assertEquals(
                    "9a44941679e7111fe3d35409017ba488a8f592b8a8692c91926d3e914a3ada23",
                    result.contentHash!!.toHexString()
                )
            }
        }
    }
}
