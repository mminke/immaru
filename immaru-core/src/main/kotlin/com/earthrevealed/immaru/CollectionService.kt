package com.earthrevealed.immaru

import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.Collection
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.MediaType
import com.earthrevealed.immaru.domain.asset
import com.earthrevealed.immaru.exceptions.AssetNotFoundException
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.apache.tika.mime.MediaType as TikaMediaType

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

object tika: Tika()

class Importer(val libraryPath: Path,
               val fileContent: ByteArray,
               val filename: String,
               val save: (Asset) -> Unit) {

    infix fun into(collection: Collection): Asset {
        val mediaType = fileContent.mediaType()
        require(mediaType.isSupported())

        val asset = asset(collection.id) {
            this.originalFilename = filename
            this.mediaType = MediaType(mediaType.toString())
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
                .filter { it.toFile().isFile}
                .map { it to it.mediaType() }
                .filter { it.second.isSupported() }
                .map { (it.first to asset(collection.id) {
                    originalFilename = it.first.fileName.toString()
                    mediaType = MediaType(it.second.toString())
                }) }
                .forEach { (source, asset) ->
                    val destination = libraryPath
                            .resolve(asset.internalFilelocation())

                    println("Copying $source TO $destination")
                    Files.createDirectories(libraryPath.resolve(asset.destinationFolders()))
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES)

                    save(asset)
                }
    }
}

private fun Path.mediaType() = this.toFile().readBytes().mediaType()

private fun TikaMediaType.isSupported() = this.type == "image" || this.type == "video"

private fun ByteArray.mediaType() = TikaMediaType.parse(tika.detect(this))
