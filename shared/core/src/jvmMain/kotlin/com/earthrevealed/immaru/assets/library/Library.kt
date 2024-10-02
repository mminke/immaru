package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.asSink
import mu.KotlinLogging
import org.apache.tika.Tika
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class Library(private val libraryRoot: Path) {

    init {
        logger.info { "Using library root: $libraryRoot" }

        Files.createDirectories(libraryRoot)
    }

    fun writeContentForAsset(asset: FileAsset, contentSource: Source): MediaType {
        return runBlocking(Dispatchers.IO) {
            Files.createDirectories(absoluteDestinationFolderFor(asset))
            // TODO: Check the file does not yet exist for the id (ignore extension)

            val absoluteFileLocation = absoluteFileLocationFor(asset)
            Files.newOutputStream(absoluteFileLocation).use { os ->
                //TODO: While saving create a SHA-1 checksum
                contentSource.transferTo(os.asSink())
            }

            //TODO: If possible also determine media type using the contentSource
            val detectedMediaType = Files.newInputStream(absoluteFileLocation).use { fis ->
                TikaInputStream.get(fis).use { tis ->
                    val detectedMimeType = Tika().detect(tis, Metadata())
                    val mediaType = MediaType.parse(detectedMimeType.toString())
                    logger.info { "Detected $mediaType" }
                    mediaType
                }
            }

            logger.debug { "Finished import asset [${asset.id}]" }
            detectedMediaType
        }
    }

    private fun absoluteFileLocationFor(asset: FileAsset): Path =
        libraryRoot.resolve(asset.internalFilelocation())

    private fun absoluteDestinationFolderFor(asset: FileAsset): Path =
        libraryRoot.resolve(asset.destinationFolder())

}
