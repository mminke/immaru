package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.common.io.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import mu.KotlinLogging

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

    fun writeContentForAsset(asset: FileAsset, content: Flow<ByteArray>) {
        val absoluteFileLocation = absoluteFileLocationFor(asset)

        require(!absoluteFileLocation.exists()) { "File already exists. overwriting content is not allowed." }

        SystemFileSystem.createDirectories(Path(absoluteDestinationFolderFor(asset).toString()))

        SystemFileSystem.sink(Path(absoluteFileLocation.toString()))
            .buffered()
            .use { fileSink ->
                runBlocking {
                    content
                        .collect { buffer ->
                            fileSink.write(buffer)
                        }
                }
            }

        logger.info { "Finished writing content for ${asset.originalFilename}" }
    }

    fun absoluteFileLocationFor(asset: FileAsset): Path =
        Path(libraryRoot, asset.internalFilelocation().toString())

    private fun absoluteDestinationFolderFor(asset: FileAsset): Path =
        Path(libraryRoot, asset.destinationFolder().toString())


    private fun Path.exists(): Boolean {
        return SystemFileSystem.metadataOrNull(this) != null
    }
}
