package com.earthrevealed.immaru.metadata

import com.earthrevealed.immaru.common.LibraryPath
import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.ImageHeight
import com.earthrevealed.immaru.domain.ImageWidth
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.OriginalDateOfCreation
import mu.KotlinLogging
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class MetadataService(
        val libraryPath: LibraryPath
) {
    private val tika = Tika()

    fun process(asset: Asset) {
        val metadata = asset.metadata()

        when(asset.mediaType.type) {
            MEDIATYPE_IMAGE.type -> processImage(asset as Image, metadata)
            MEDIATYPE_VIDEO.type -> TODO()
        }
    }

    private fun Asset.metadata(): Metadata {
        val metadata = Metadata()
        tika.parse(libraryPath.absolutePathFor(this), metadata)
        return metadata
    }

    private fun processImage(image: Image, metadata: Metadata) {
        image.originalCreatedAt = OriginalDateOfCreation.of(metadata.orignalCreationDate())
        image.imageWidth = ImageWidth.of(metadata.imageWidth())
        image.imageHeight = ImageHeight.of(metadata.imageHeight())
    }

    private fun Metadata.orignalCreationDate(): String {
        return this[TikaCoreProperties.CREATED]?:"0001-01-01T00:00:00"
    }

    private fun Metadata.imageWidth(): String {
        return this["Image Width"]?: this["tiff:ImageWidth"]?: this["Exif SubIFD:Image Width"]?: this["width"]?: "-1"
    }
    private fun Metadata.imageHeight(): String {
        return this["Image Height"]?: this["tiff:ImageLength"]?: this["Exif SubIFD:Image Height"]?: this["height"]?: "-1"
    }
}