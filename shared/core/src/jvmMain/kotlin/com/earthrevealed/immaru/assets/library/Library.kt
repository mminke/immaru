package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.io.HashSink
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

private val logger = KotlinLogging.logger { }

class Library(private val libraryRoot: Path) {

    init {
        logger.info { "Using library root: $libraryRoot" }

        SystemFileSystem.createDirectories(libraryRoot)
    }

    fun readContentForAsset(asset: FileAsset): Source {
        return SystemFileSystem.source(
            Path(
                absoluteFileLocationFor(asset).toString()
            )
        ).buffered()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun writeContentForAsset(asset: FileAsset, contentSource: Source): WriteResult {
        return runBlocking(Dispatchers.IO) {
            val absoluteFileLocation = absoluteFileLocationFor(asset)

            require(!absoluteFileLocation.exists()) { "File already exists. overwriting content is not allowed." }

            SystemFileSystem.createDirectories(Path(absoluteDestinationFolderFor(asset).toString()))


            val hashSink = HashSink()
            SystemFileSystem.sink(Path(absoluteFileLocation.toString())).use { fileSink ->
                hashSink.pipeTo(fileSink)
                hashSink.use {
                    contentSource.transferTo(it)
                }
            }

            val detectedMediaType =
                SystemFileSystem.source(Path(absoluteFileLocation.toString()))
                    .buffered()
                    .asInputStream().use { sis ->
                        TikaInputStream.get(sis).use { tis ->
                            val detectedMimeType = Tika().detect(tis, Metadata())
                            val mediaType = MediaType.parse(detectedMimeType.toString())
                            logger.info { "Detected $mediaType" }
                            mediaType
                        }
                    }

            logger.debug { "Finished import asset [id=${asset.id}, sha256=${hashSink.hashValue!!.toHexString()}]" }

            WriteResult(
                detectedMediaType,
                hashSink.hashValue!!
            )
        }
    }

    data class WriteResult(
        val mediaType: MediaType,
        val contentHash: ByteArray
    )

    private fun absoluteFileLocationFor(asset: FileAsset): Path =
        Path(libraryRoot, asset.internalFilelocation().toString())

    private fun absoluteDestinationFolderFor(asset: FileAsset): Path =
        Path(libraryRoot, asset.destinationFolder().toString())


    private fun Path.exists(): Boolean {
        return SystemFileSystem.metadataOrNull(this) != null
    }
}
