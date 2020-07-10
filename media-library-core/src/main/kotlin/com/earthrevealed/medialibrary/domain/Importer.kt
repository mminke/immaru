package com.earthrevealed.medialibrary.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Component
class Importer(
        @Value("media-library.library.path") libraryPathValue: String
) {
    private val libraryPath: Path

    init {
        libraryPath = Path.of(libraryPathValue)
        Files.createDirectories(libraryPath)
    }

    fun importFrom(importLocation: Path) {
        Files.walk(importLocation)
                .filter { isImage(it) || isVideo(it) }
                .map { (it to Media(it)) }
                .forEach { (source, media) ->
                    println("Importing: $media")
//                    val contentType = Files.probeContentType(source)
                    val extension = source.extension()?.toLowerCase() ?: ""

                    val destinationPath = libraryPath
                            .resolve(media.folders())
                    val destination = destinationPath
                            .resolve("${media.id}.${extension}")

                    println("Copying $source TO $destination")
                    Files.createDirectories(destinationPath)
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES)
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


fun Media.folders(): Path {
    var subFolders = Path.of("")
    (0..3).forEach {
        subFolders = subFolders.resolve(id.toString().substring((it * 2) + 0..(it * 2) + 1))
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