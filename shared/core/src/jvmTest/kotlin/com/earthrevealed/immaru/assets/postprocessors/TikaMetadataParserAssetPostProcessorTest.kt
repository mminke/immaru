package com.earthrevealed.immaru.assets.postprocessors

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.test.utils.resourceAsPath
import com.earthrevealed.immaru.assets.test.utils.useResourceAsFlow
import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class TikaMetadataParserAssetPostProcessorTest {

    @Test
    fun `test parsing metadata using the tika post processor`() {
        val postProcessor = TikaMetadataParserAssetPostProcessor()

        val path = resourceAsPath("1px.jpg")
        val asset = FileAsset(
            collectionId = CollectionId(),
            originalFilename = "1px.jpg",
        )

        postProcessor.postProcess(asset, path)

        assertEquals(null,asset.originalCreatedAt)
    }
}
