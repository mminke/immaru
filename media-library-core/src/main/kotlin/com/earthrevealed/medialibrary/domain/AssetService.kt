package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.persistence.AssetRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class AssetService(
        @Value("\${media-library.library.path}") libraryPathValue: String,
        val assetRepository: AssetRepository
) {
    private val libraryPath: Path

    init {
        libraryPath = Path.of(libraryPathValue)
        Files.createDirectories(libraryPath)
    }

    fun importFrom(importLocation: Path) {
        Files.walk(importLocation)
                .filter { isImage(it) || isVideo(it) }
                .map { (it to asset { originalFilename = it.fileName.toString() }) }
                .forEach { (source, asset) ->
                    println("Importing: $asset")
//                    val contentType = Files.probeContentType(source)

                    val destination = libraryPath
                            .resolve(asset.internalFilelocation())

                    println("Copying $source TO $destination")
                    Files.createDirectories(libraryPath.resolve(asset.destinationFolders()))
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES)

                    assetRepository.save(asset)
                }
    }

    fun assetPath(id: AssetId) =
        assetRepository.get(id)?.let {
            libraryPath.resolve(it.internalFilelocation())
        }


    private fun isImage(path: Path): Boolean {
        return path.toString().toLowerCase().endsWith("jpg") ||
                path.toString().toLowerCase().endsWith("jpeg") ||
                path.toString().toLowerCase().endsWith("png") ||
                path.toString().toLowerCase().endsWith("cr2")
    }

    private fun isVideo(path: Path): Boolean {
        return path.toString().toLowerCase().endsWith("avi") ||
                path.toString().toLowerCase().endsWith("mov")
    }
}

private fun Path.extension(): String? {
    val asString = this.toString()
    val index = asString.lastIndexOf('.')
    if (index > 0) {
        return asString.substring(index + 1)
    }
    return null
}