package com.earthrevealed.immaru

import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.Collection
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.asset
import com.earthrevealed.immaru.exceptions.AssetNotFoundException
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class CollectionNotEmptyException(message: String?) : RuntimeException(message)

@Service
class CollectionService(
        @Value("\${immaru.library.path}") libraryPathValue: String,
        val collectionRepository: CollectionRepository,
        val assetRepository: AssetRepository
) {
    private val libraryPath: Path

    init {
        libraryPath = Path.of(libraryPathValue)
        Files.createDirectories(libraryPath)
    }

    fun save(collection: Collection) {
        collectionRepository.save(collection)
    }

    fun delete(collection: Collection) {
        if(collection.containsAssets()) {
            throw CollectionNotEmptyException("Cannot delete collection because it contains assets.")
        }
        collectionRepository.delete(collection)
    }

    private fun Collection.containsAssets(): Boolean {
        return assetRepository.hasAssets(this.id)
    }

    fun import(fileContent: ByteArray, filename: String): Importer
        = Importer(libraryPath, fileContent, filename) { assetRepository.save(it)}

    fun importFrom(importLocation: Path): PathImporter
        = PathImporter(libraryPath, importLocation) { assetRepository.save(it) }

    fun assetPath(collectionId: CollectionId, id: AssetId): Path =
        assetRepository.get(collectionId, id)?.let {
            libraryPath.resolve(it.internalFilelocation())
        }?: throw AssetNotFoundException(collectionId, id)
}

private fun Path.extension(): String? {
    val asString = this.toString()
    val index = asString.lastIndexOf('.')
    if (index > 0) {
        return asString.substring(index + 1)
    }
    return null
}

class Importer(val libraryPath: Path,
               val fileContent: ByteArray,
               val filename: String,
               val save: (Asset) -> Unit) {

    infix fun into(collection: Collection): Asset {
        val asset = asset(collection.id) {
            originalFilename = filename
        }
        val destination = libraryPath
                .resolve(asset.internalFilelocation())

        Files.createDirectories(libraryPath.resolve(asset.destinationFolders()))
        Files.write(destination, fileContent)

        save(asset)

        return asset
    }
}

class PathImporter(val libraryPath: Path, val importLocation: Path, val save: (Asset) -> Unit ) {
    infix fun into(collection: Collection) {
        Files.walk(importLocation)
                .filter { isImage(it) || isVideo(it) }
                .map { (it to asset(collection.id) {
                    originalFilename = it.fileName.toString()
                }) }
                .forEach { (source, asset) ->
                    println("Importing: $asset")
//                    val contentType = Files.probeContentType(source)

                    val destination = libraryPath
                            .resolve(asset.internalFilelocation())

                    println("Copying $source TO $destination")
                    Files.createDirectories(libraryPath.resolve(asset.destinationFolders()))
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES)

                    save(asset)
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