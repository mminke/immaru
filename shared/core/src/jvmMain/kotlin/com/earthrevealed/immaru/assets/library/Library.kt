package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    fun writeContentForAsset(asset: FileAsset, binaryDataFlow: Flow<ByteArray>): MediaType {
        check(asset.mediaTypeIsNotDefined) { "Media type already defined. [assetId=${asset.id}" }

        return runBlocking(Dispatchers.IO) {
            // TODO: Check the file does not yet exist for the id (ignore extension)

            Files.createDirectories(absoluteDestinationFolderFor(asset))
            val absoluteFileLocation = absoluteFileLocationFor(asset)

            Files.newOutputStream(absoluteFileLocation).use { os ->
                binaryDataFlow.collect { bytes ->
                    withContext(Dispatchers.IO) {
                        os.write(bytes)

                        //TODO: While saving create a SHA-1 checksum
                    }
                }
            }

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

    fun fileForAsset(asset: FileAsset): File {
        return absoluteFileLocationFor(asset).toFile()
    }

    private fun absoluteFileLocationFor(asset: FileAsset): Path =
        libraryRoot.resolve(asset.internalFilelocation())

    private fun absoluteDestinationFolderFor(asset: FileAsset): Path =
        libraryRoot.resolve(asset.destinationFolder())

}
