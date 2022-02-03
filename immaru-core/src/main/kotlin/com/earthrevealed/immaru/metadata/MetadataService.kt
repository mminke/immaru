package com.earthrevealed.immaru.metadata

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FrameRate
import com.earthrevealed.immaru.assets.Height
import com.earthrevealed.immaru.assets.Image
import com.earthrevealed.immaru.assets.OriginalDateOfCreation
import com.earthrevealed.immaru.assets.Video
import com.earthrevealed.immaru.assets.Width
import com.earthrevealed.immaru.common.LibraryPath
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

    fun process(image: Image) {
        val metadata = image.metadata()

        image.originalCreatedAt = OriginalDateOfCreation.of(metadata.orignalCreationDate())
        image.width = Width.of(metadata.imageWidth())
        image.height = Height.of(metadata.imageHeight())
    }

    fun process(video: Video) {
        val metadata = video.metadata()

        video.originalCreatedAt = OriginalDateOfCreation.of(metadata.orignalCreationDate())
        video.width = Width.of(metadata.imageWidth())
        video.height = Height.of(metadata.imageHeight())
        video.frameRate = FrameRate.UNKNOWN
    }

    private fun Asset.metadata(): Metadata {
        val metadata = Metadata()
        tika.parse(libraryPath.absolutePathFor(this), metadata)
        return metadata
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