package com.earthrevealed.immaru

import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.common.LibraryPath
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_JPEG
import com.earthrevealed.immaru.domain.collection
import com.earthrevealed.immaru.metadata.MetadataService
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import org.amshove.kluent.`should be equal to`
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
    @Mock
    lateinit var metadataServiceMock: MetadataService

    @BeforeEach
    fun initializeClock() {
        ClockProvider(Clock.systemDefaultZone())
    }

//    @Test
//    fun `test importing files from a given path`() {
//        val collectionService = CollectionService(
//                LibraryPath(temporaryFolder.toString()),
//                collectionRepositoryMock,
//                assetRepositoryMock,
//                metadataServiceMock
//        )
//        val collection = collection { name = "Default collection" }
//
//        collectionService.importFrom(Path.of("./src/test/resources/images")) into collection
//
//        assertThat {
//            Files.walk(temporaryFolder)
//                    .filter { Files.isRegularFile(it) }
//                    .collect(Collectors.toList())
//                    .size `should be equal to` 3
//        }
//
//        verify(assetRepositoryMock, times(3)).save(any())
//    }

    @Test
    fun `test importing a file as binary content`() {
        val collectionService = CollectionService(
                LibraryPath(temporaryFolder.toString()),
                collectionRepositoryMock,
                assetRepositoryMock,
                metadataServiceMock
        )
        val collection = collection { name = "Default collection" }
        val fileContent = resource("/images/P7310035.JPG").readBytes()

        val asset = collectionService.import(fileContent, "test.jpg") into collection

        assertThat {
            asset.originalFilename `should be equal to` "test.jpg"
            asset.collectionId `should be equal to` collection.id
            asset.mediaType `should be equal to` MEDIATYPE_IMAGE_JPEG

            val file = Files.walk(temporaryFolder)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
                    .single()

            file.fileName.toString() `should be equal to` asset.internalFilename()
            Files.readAllBytes(file) `should be equal to` fileContent
        }
    }

    fun resource(filename: String) = this::class.java.getResource(filename)
}

fun assertThat(action: () -> Unit) {
    action()
}