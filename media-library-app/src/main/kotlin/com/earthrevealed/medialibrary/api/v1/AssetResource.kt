package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.CollectionService
import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.persistence.AssetRepository
import com.earthrevealed.medialibrary.persistence.CollectionRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    fun assets(@PathVariable("collectionId") collectionId: CollectionId): List<Asset> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        return assetRepository.all(collectionId)
    }


    @GetMapping("/assets/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun asset(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") id: AssetId
    ): Asset? {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        return assetRepository.get(collectionId, id)
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

    @PostMapping("/assets")
    fun createAsset(
            @PathVariable("collectionId") collectionId: CollectionId,
            @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<AssetCreationResult> {
        if(collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }
        val collection = collectionRepository.get(collectionId)!!

        val createdAssets = mutableSetOf<Asset>()
        files
                .filter { !it.isEmpty }
                .forEach {
                    println("uploaded file: ${it.originalFilename}")
                    createdAssets.add(collectionService.import(it.bytes, it.originalFilename!!) into collection)
                }

        val locations = createdAssets
                .map { "/collections/${collectionId.value}/assets/${it.id.value}" }

        return ResponseEntity<AssetCreationResult>(AssetCreationResult(locations), HttpStatus.CREATED)
    }
}

data class AssetCreationResult(val locations: List<String>)