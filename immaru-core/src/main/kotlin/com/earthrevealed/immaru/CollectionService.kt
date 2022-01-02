package com.earthrevealed.immaru

import com.earthrevealed.immaru.common.LibraryPath
import com.earthrevealed.immaru.common.isSupported
import com.earthrevealed.immaru.common.mediaType
import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.Collection
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.MediaType
import com.earthrevealed.immaru.domain.Video
import com.earthrevealed.immaru.domain.image
import com.earthrevealed.immaru.domain.video
import com.earthrevealed.immaru.exceptions.AssetNotFoundException
import com.earthrevealed.immaru.metadata.MetadataService
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import org.springframework.stereotype.Service
import java.nio.file.Path

class CollectionNotEmptyException(message: String?) : RuntimeException(message)

@Service
class CollectionService(
        val libraryPath: LibraryPath,
        val collectionRepository: CollectionRepository,
        val assetRepository: AssetRepository,
        val metadataService: MetadataService
) {
    fun save(collection: Collection) {
        collectionRepository.save(collection)
    }

    fun delete(collection: Collection) {
        if (collection.containsAssets()) {
            throw CollectionNotEmptyException("Cannot delete collection because it contains assets.")
        }
        collectionRepository.delete(collection)
    }

    private fun Collection.containsAssets(): Boolean {
        return assetRepository.hasAssets(this.id)
    }

    fun import(fileContent: ByteArray, filename: String): Importer =
            Importer(libraryPath, fileContent, filename) {
                if(it is Image) {
                    metadataService.process(it)
                    assetRepository.save(it)
                } else if(it is Video) {
                    metadataService.process(it)
                    assetRepository.save(it)
                }
            }

//    fun importFrom(importLocation: Path): PathImporter
//        = PathImporter(libraryPath, importLocation) { assetRepository.save(it) }

    fun assetPath(collectionId: CollectionId, id: AssetId): Path =
            assetRepository.get(collectionId, id)?.let { asset ->
                libraryPath.absolutePathFor(asset)
            } ?: throw AssetNotFoundException(collectionId, id)
}

private fun Path.extension(): String? {
    val asString = this.toString()
    val index = asString.lastIndexOf('.')
    if (index > 0) {
        return asString.substring(index + 1)
    }
    return null
}

class Importer(val libraryPath: LibraryPath,
               val fileContent: ByteArray,
               val filename: String,
               val save: (Asset) -> Unit) {

    infix fun into(collection: Collection): Asset {
        val rawMediaType = fileContent.mediaType()
        require(rawMediaType.isSupported())

        val mediaType = MediaType.valueOf(rawMediaType.toString())
        val asset = when (mediaType.type) {
            MEDIATYPE_IMAGE.type -> image(collection.id) {
                this.originalFilename = filename
                this.mediaType = mediaType
            }
            MEDIATYPE_VIDEO.type -> video(collection.id) {
                this.originalFilename = filename
                this.mediaType = mediaType
            }
            else -> throw IllegalArgumentException("Media type not supported: ${mediaType}")
        }

        libraryPath.write(asset, fileContent)

        save(asset)

        return asset
    }

}

//class PathImporter(val libraryPath: LibraryPath, val importLocation: Path, val save: (Asset) -> Unit ) {
//    infix fun into(collection: Collection) {
//        Files.walk(importLocation)
//                .filter { it.toFile().isFile}
//                .map { it to it.mediaType() }
//                .filter { it.second.isSupported() }
//                .map { (it.first to asset(collection.id) {
//                    originalFilename = it.first.fileName.toString()
//                    mediaType = MediaType(it.second.toString())
//                }) }
//                .forEach { (source, asset) ->
//                    libraryPath.copyFileToAsset(source, asset)
//
//                    save(asset)
//                }
//    }
//}
