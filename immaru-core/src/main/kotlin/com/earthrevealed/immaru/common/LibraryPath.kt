package com.earthrevealed.immaru.common

import com.earthrevealed.immaru.domain.Asset
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Component
class LibraryPath(@Value("\${immaru.library.path}") value: String) {
    private val libraryRoot: Path

    init {
        libraryRoot = Path.of(value)
        Files.createDirectories(libraryRoot)
    }

    fun absolutePathFor(asset: Asset): Path = libraryRoot.resolve(asset.internalFilelocation())

    fun absoluteDestinationFolderFor(asset: Asset): Path = libraryRoot.resolve(asset.destinationFolder())

    fun write(asset: Asset, fileContent: ByteArray) {
        Files.createDirectories(absoluteDestinationFolderFor(asset))
        val absolutePath = absolutePathFor(asset)
        Files.write(absolutePath, fileContent)
    }

    fun copyFileToAsset(source: Path, asset: Asset) {
        Files.createDirectories(absoluteDestinationFolderFor(asset))
        Files.copy(source, absolutePathFor(asset), StandardCopyOption.COPY_ATTRIBUTES)
    }
}