package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.persistence.exposed.AssetTable
import com.earthrevealed.medialibrary.persistence.exposed.from
import com.earthrevealed.medialibrary.persistence.exposed.toDomain
import com.earthrevealed.medialibrary.persistence.exposed.toEntityId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class AssetRepository {
    fun save(asset: Asset) {
        AssetTable.insert { it.from(asset) }
    }

    fun all() =
        AssetTable.selectAll()
                .map { it.toDomain() }

    fun get(id: AssetId) =
        AssetTable.select { AssetTable.id eq id.toEntityId() }
                .firstOrNull()?.let { it.toDomain() }
}