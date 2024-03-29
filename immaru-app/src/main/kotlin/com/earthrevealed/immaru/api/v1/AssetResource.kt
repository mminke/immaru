package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.CollectionService
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.Group
import com.earthrevealed.immaru.assets.Image
import com.earthrevealed.immaru.assets.Video
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_GIF
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_JPEG
import com.earthrevealed.immaru.domain.MEDIATYPE_IMAGE_PNG
import com.earthrevealed.immaru.domain.MEDIATYPE_VIDEO
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.image.convertToPng
import com.earthrevealed.immaru.image.scaleImage
import com.earthrevealed.immaru.persistence.CollectionRepository
import com.earthrevealed.immaru.video.extractThumbnail
import org.apache.commons.io.IOUtils
import org.springframework.http.CacheControl
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
import java.time.Duration
import javax.servlet.http.HttpServletResponse
import com.earthrevealed.immaru.domain.MediaType as ImmaryMediaType

@RestController
@RequestMapping("/collections/{collectionId}")
class AssetResource(
    val assetRepository: AssetRepository,
    val collectionService: CollectionService,
    val collectionRepository: CollectionRepository
) {

    @GetMapping("/assets", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun assets(
        @PathVariable("collectionId") collectionId: CollectionId,
        @RequestParam("tagIds", required = false) tagIds: Set<TagId>?
    ): List<Asset> {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }
        return assetRepository.findByTags(collectionId, tagIds)
    }

    @GetMapping("/assets/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun asset(
        @PathVariable("collectionId") collectionId: CollectionId,
        @PathVariable("id") id: AssetId
    ): Asset {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }
        return assetRepository.get(collectionId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${id.value} does not exist.")
    }

    @GetMapping("/assets/{id}/content")
    fun assetContent(
        @PathVariable("collectionId") collectionId: CollectionId,
        @PathVariable("id") id: AssetId,
        response: HttpServletResponse
    ) {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }
        val asset = assetRepository.get(collectionId, id)
        if (asset == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${id.value} does not exist.")
        }

        val fileLocation = collectionService.assetPath(collectionId, id)

        response.setCacheControl(Duration.ofHours(24))
        Files.newInputStream(fileLocation).use { inputStream ->
            if (isSupported(asset.mediaType)) {
                response.contentType = asset.mediaType.toString()
                IOUtils.copy(inputStream, response.outputStream)
                response.flushBuffer()
            } else {
                convertToPng(inputStream, response.outputStream)
            }
        }
    }

    private fun HttpServletResponse.setCacheControl(maxAge: Duration) {
        this.addHeader("Cache-Control", "max-age=${maxAge.toSeconds()}, no-transform")
    }

    private fun isSupported(mediaType: ImmaryMediaType) =
        mediaType == MEDIATYPE_IMAGE_JPEG || mediaType == MEDIATYPE_IMAGE_PNG || mediaType == MEDIATYPE_IMAGE_GIF
                || mediaType.isCompatible(MEDIATYPE_VIDEO)

    @GetMapping("/assets/{id}/thumbnail")
    fun assetThumbnail(
        @PathVariable("collectionId") collectionId: CollectionId,
        @PathVariable("id") id: AssetId
    ): ResponseEntity<ByteArray> {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }
        val asset = assetRepository.get(collectionId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${id.value} does not exist.")

        val outputStream = when (asset) {
            is Image -> {
                val fileLocation = collectionService.assetPath(collectionId, id)

                scaleImage(Files.newInputStream(fileLocation))
            }

            is Video -> {
                val path = collectionService.assetPath(collectionId, id)

                extractThumbnail(path)
            }

            is Group -> {
                // Get first asset of group and create thumbnail for that
                TODO("Not implemented yet")
            }
        }

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.IMAGE_PNG
        responseHeaders.setCacheControl(
            CacheControl.maxAge(Duration.ofHours(24))
        )

        val imageByteArray = outputStream.toByteArray()
        return ResponseEntity(imageByteArray, responseHeaders, HttpStatus.OK)
    }

    @PostMapping("/assets")
    fun createAsset(
        @PathVariable("collectionId") collectionId: CollectionId,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<CreationResult> {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }
        val collection = collectionRepository.get(collectionId)!!

        val createdAssets = files
            .filter { !it.isEmpty }
            .map { collectionService.import(it.bytes, it.originalFilename!!) into collection }

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
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Collection with id ${collectionId.value} does not exist."
            )
        }

        val asset = assetRepository.get(collectionId, assetId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Asset with id ${assetId.value} does not exist.")
        val updatedAsset = when (asset.mediaType.type) {
            MEDIATYPE_IMAGE.type -> (asset as Image).copy(tagIds = assetTagIds)
            MEDIATYPE_VIDEO.type -> TODO()
            else -> throw IllegalStateException("Unsupported media type for asset with id ${asset.id}")
        }
        assetRepository.updateTagsFor(updatedAsset)

        return ResponseEntity("{}", HttpStatus.ACCEPTED)
    }
}
