package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.persistence.exposed.AssetTable
import com.earthrevealed.medialibrary.persistence.exposed.from
import org.jetbrains.exposed.sql.insert
import org.springframework.stereotype.Repository

@Repository
class AssetRepository {
    fun save(asset: Asset) {
        AssetTable.insert { it.from(asset) }
    }
}