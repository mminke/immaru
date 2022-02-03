package com.earthrevealed.immaru.metadata

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.image
import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.common.LibraryPath
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_JPEG
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.util.*

internal class MetadataServiceTest {

    @BeforeEach
    fun initializeClock() {
        ClockProvider(Clock.systemDefaultZone())
    }

    @Test
    fun test() {
        val libraryPath = LibraryPath("./src/test/resources/images")
        val metadataService = MetadataService(libraryPath)

        val image = image(CollectionId(UUID.randomUUID())) {
            id = AssetId( value = UUID.fromString("47d055e5-08fd-476e-a7b7-d29d7fdedcdd"))
            originalFilename = "P7310035.jpg"
            mediaType = MEDIATYPE_IMAGE_JPEG
        }

        metadataService.process(image)
    }
}