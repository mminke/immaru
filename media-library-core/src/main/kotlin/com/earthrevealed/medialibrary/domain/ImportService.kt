package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.persistence.AssetRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class ImportService(
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
                    val extension = source.extension()?.toLowerCase() ?: ""

                    val destinationPath = libraryPath
                            .resolve(asset.destinationFolders())
                    val destination = destinationPath
                            .resolve("${asset.id}.${extension}")

                    println("Copying $source TO $destination")
                    Files.createDirectories(destinationPath)
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES)

                    assetRepository.save(asset)
                }
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

/**
 * Determine the destination folder to store the asset in.
 * Sub folders are determined by the first 6 digits of the UUID:
 * id = a4e6d238-39eb-4efc-b23d-be6ac0f05e75
 * destination folder = /a4/e6/d2/38/
 */
fun Asset.destinationFolders(): Path {
    var subFolders = Path.of("")
    (0..3).forEach {
        val offset = (it * 2)
        subFolders = subFolders.resolve(id.value.toString().substring(offset + 0..offset + 1))
    }
    return subFolders
}

private fun Path.extension(): String? {
    val asString = this.toString()
    val index = asString.lastIndexOf('.')
    if (index > 0) {
        return asString.substring(index + 1)
    }
    return null
}