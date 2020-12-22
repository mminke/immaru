package com.earthrevealed.immaru

import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.domain.collection
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
internal class CollectionServiceTest {
    @TempDir
    lateinit var temporaryFolder: Path

    @Mock
    lateinit var assetRepositoryMock: AssetRepository
    @Mock
    lateinit var collectionRepositoryMock: CollectionRepository

    @BeforeEach
    fun initializeClock() {
        ClockProvider(Clock.systemDefaultZone())
    }

    @Test
    fun `test importing files from a given path`() {
        val collectionService = CollectionService(temporaryFolder.toString(), collectionRepositoryMock, assetRepositoryMock)
        val collection = collection { name = "Default collection" }

        collectionService.importFrom(Path.of("./src/test/resources/images")) into collection

        assertThat {
            Files.walk(temporaryFolder)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
                    .size `should be equal to` 3
        }

        verify(assetRepositoryMock, times(3)).save(any())
    }

    @Test
    fun `test importing a file as binary content`() {
        val collectionService = CollectionService(temporaryFolder.toString(), collectionRepositoryMock, assetRepositoryMock)
        val collection = collection { name = "Default collection" }
        val fileContent = "Dit is een testfile".toByteArray()

        val asset = collectionService.import(fileContent, "test.jpg") into collection

        assertThat {
            asset.originalFilename `should be equal to` "test.jpg"
            asset.collectionId `should be equal to` collection.id

            val file = Files.walk(temporaryFolder)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
                    .single()

            file.fileName.toString() `should be equal to` asset.internalFilename()
            Files.readAllBytes(file) `should be equal to` fileContent
        }
    }
}

fun assertThat(action: () -> Unit) {
    action()
}