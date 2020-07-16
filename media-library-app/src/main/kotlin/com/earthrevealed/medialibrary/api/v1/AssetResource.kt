package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.persistence.AssetRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class AssetResource(
        val assetRepository: AssetRepository
) {

    @GetMapping("/assets")
    fun assets() =
            assetRepository.all()
}