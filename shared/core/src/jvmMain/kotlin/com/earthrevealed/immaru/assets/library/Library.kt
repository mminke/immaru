package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import mu.KotlinLogging
import org.apache.tika.Tika
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import kotlinx.io.files.Path as KotlinxIoPath

private val logger = KotlinLogging.logger { }

class Library(private val libraryRoot: Path) {

    init {
        logger.info { "Using library root: $libraryRoot" }

        SystemFileSystem.createDirectories(libraryRoot)
    }

    fun readContentForAsset(asset: FileAsset): Source {
        return SystemFileSystem.source(
            KotlinxIoPath(
                absoluteFileLocationFor(asset).toString()
            )
        ).buffered()
    }

    fun writeContentForAsset(asset: FileAsset, contentSource: Source): MediaType {
        return runBlocking(Dispatchers.IO) {
            SystemFileSystem.createDirectories(KotlinxIoPath(absoluteDestinationFolderFor(asset).toString()))

            // TODO: Check the file does not yet exist for the id (ignore extension)
            val absoluteFileLocation = absoluteFileLocationFor(asset)

            SystemFileSystem.sink(KotlinxIoPath(absoluteFileLocation.toString())).use {
                //TODO: While saving create a SHA-1 checksum
                contentSource.transferTo(it)
            }

            //TODO: If possible also determine media type using the contentSource
            val detectedMediaType =
                SystemFileSystem.source(KotlinxIoPath(absoluteFileLocation.toString()))
                    .buffered()
                    .asInputStream().use { sis ->
                        TikaInputStream.get(sis).use { tis ->
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
        Path(libraryRoot, asset.internalFilelocation().toString())

    private fun absoluteDestinationFolderFor(asset: FileAsset): Path =
        Path(libraryRoot, asset.destinationFolder().toString())

}
