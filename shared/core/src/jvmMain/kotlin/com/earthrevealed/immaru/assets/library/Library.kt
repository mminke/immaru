package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.common.io.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
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
import java.security.MessageDigest

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

    fun readContentForAssetAsFlow(asset: FileAsset): Flow<ByteArray> {
        return SystemFileSystem.source(
            Path(
                absoluteFileLocationFor(asset).toString()
            )
        ).buffered().toFlow()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun writeContentForAsset(asset: FileAsset, content: Flow<ByteArray>): WriteResult {
        val algorithm = "SHA-256"
        val messageDigest = MessageDigest.getInstance(algorithm)

        val absoluteFileLocation = absoluteFileLocationFor(asset)

        require(!absoluteFileLocation.exists()) { "File already exists. overwriting content is not allowed." }

        SystemFileSystem.createDirectories(Path(absoluteDestinationFolderFor(asset).toString()))

        // Write contents to file
        SystemFileSystem.sink(Path(absoluteFileLocation.toString()))
            .buffered()
            .use { fileSink ->
                runBlocking {
                    content
                        .onEach {
                            messageDigest.update(it)
                        }
                        .onEach {
                            //TODO: Determine media type while processing the flow
                        }
                        .collect { buffer ->
                            fileSink.write(buffer)
                        }

                    fileSink.close()
                }
            }

        val hashValue = messageDigest.digest()

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

        logger.debug { "Finished import asset [id=${asset.id}, sha256=${hashValue.toHexString()}, mediaType=$detectedMediaType]" }

        return WriteResult(
            detectedMediaType,
            hashValue
        )
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
