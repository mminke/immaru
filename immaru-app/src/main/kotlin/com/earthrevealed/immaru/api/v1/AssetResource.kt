package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.CollectionService
import com.earthrevealed.immaru.domain.Asset
import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Image
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.image.scaleImage
import com.earthrevealed.immaru.persistence.AssetRepository
import com.earthrevealed.immaru.persistence.CollectionRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files

@RestController
@RequestMapping("/collections/{collectionId}")
class AssetResource(
        val assetRepository: AssetRepository,
        val collectionService: CollectionService,
        val collectionRepository: CollectionRepository
) {

    @GetMapping("/assets", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun assets(
            @PathVariable("collectionId") collectionId: CollectionId
    ): List<Asset> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        return assetRepository.all(collectionId)
    }

    @GetMapping("/assets/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun asset(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") id: AssetId
    ): Asset {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        return assetRepository.get(collectionId, id)?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${id.value} does not exist.")
    }

    @GetMapping("/assets/{id}")
    fun assetContent(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") id: AssetId
    ): ResponseEntity<ByteArray> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        // TODO: replace both statements with collectionService.assetContents(collectionId, id)
        val fileLocation = collectionService.assetPath(collectionId, id)
        val fileContents = Files.readAllBytes(fileLocation)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.IMAGE_JPEG

        return ResponseEntity(fileContents, responseHeaders, HttpStatus.OK)
    }

    @GetMapping("/assets/{id}/thumbnail")
    fun assetThumbnail(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") id: AssetId
    ): ResponseEntity<ByteArray> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        val fileLocation = collectionService.assetPath(collectionId, id)

        val scaledImageOutputStream = scaleImage(Files.newInputStream(fileLocation))

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.IMAGE_PNG

        val imageByteArray = scaledImageOutputStream.toByteArray()
        return ResponseEntity(imageByteArray, responseHeaders, HttpStatus.OK)
    }

    @PostMapping("/assets")
    fun createAsset(
            @PathVariable("collectionId") collectionId: CollectionId,
            @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<CreationResult> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        val collection = collectionRepository.get(collectionId)!!

        val createdAssets = mutableSetOf<Asset>()
        files
                .filter { !it.isEmpty }
                .forEach {
                    createdAssets.add(collectionService.import(it.bytes, it.originalFilename!!) into collection)
                }

        val locations = createdAssets
                .map { "/collections/${collectionId.value}/assets/${it.id.value}" }

        return ResponseEntity(CreationResult(locations), HttpStatus.CREATED)
    }

    @PutMapping("/assets/{id}/tags", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateAssetTags(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") assetId: AssetId,
            @RequestBody assetTagIds: Set<TagId>
    ): ResponseEntity<String> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        val asset = assetRepository.get(collectionId, assetId)?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${assetId.value} does not exist.")
        val updatedAsset = when(asset.mediaType.type) {
            MEDIATYPE_IMAGE.type -> (asset as Image).copy(tagIds = assetTagIds)
            MEDIATYPE_VIDEO.type -> TODO()
            else -> throw IllegalStateException("Unsupported media type for asset with id ${asset.id}")
        }
        assetRepository.updateTagsFor(updatedAsset)

        return ResponseEntity("{}", HttpStatus.ACCEPTED)
    }
}
