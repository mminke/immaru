package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.AssetService
import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.persistence.AssetRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.util.*


@RestController
class AssetResource(
        val assetRepository: AssetRepository,
        val assetService: AssetService
) {

    @GetMapping("/assets")
    fun assets() =
            assetRepository.all()

    @GetMapping("/assets/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun asset(@PathVariable("id") id: UUID) =
        assetRepository.get(AssetId(id))

    @GetMapping("/assets/{id}")
    fun assetContent(@PathVariable("id") id: UUID): ResponseEntity<ByteArray> {
        val fileLocation = assetService.assetPath(AssetId(id))
        val fileContents = Files.readAllBytes(fileLocation)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.IMAGE_JPEG

        return ResponseEntity(fileContents, responseHeaders, HttpStatus.OK)
    }

    @PostMapping("/assets")
    fun createAsset(@RequestParam("files") files: List<MultipartFile>): ResponseEntity<AssetCreationResult> {
        println("Processing new asset.")
        val createdAssets = mutableSetOf<Asset>()
        files
                .filter { !it.isEmpty }
                .forEach {
                    println("uploaded file: ${it.originalFilename}")
                    createdAssets.add(assetService.import(it.bytes, it.originalFilename))
                }

        val locations = createdAssets
                .map { "/assets/${it.id.value}" }

        return ResponseEntity<AssetCreationResult>(AssetCreationResult(locations), HttpStatus.CREATED)
    }
}

data class AssetCreationResult(val locations: List<String>)