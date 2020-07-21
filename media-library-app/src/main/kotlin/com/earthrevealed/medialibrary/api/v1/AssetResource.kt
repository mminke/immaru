package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.AssetService
import com.earthrevealed.medialibrary.persistence.AssetRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
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
}