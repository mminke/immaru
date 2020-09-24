package com.earthrevealed.medialibrary.persistence

import com.earthrevealed.medialibrary.domain.Asset
import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.persistence.exposed.AssetTable
import com.earthrevealed.medialibrary.persistence.exposed.from
import com.earthrevealed.medialibrary.persistence.exposed.toAsset
import com.earthrevealed.medialibrary.persistence.exposed.toEntityId
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class AssetRepository {
    fun save(asset: Asset) {
        AssetTable
                .insert { it.from(asset) }
    }

    fun all(collectionId: CollectionId) =
            AssetTable
                    .select { AssetTable.collectionId eq collectionId.value }
                    .map { it.toAsset() }

    fun get(collectionId: CollectionId, id: AssetId) =
            AssetTable
                    .select { AssetTable.id eq id.toEntityId() }
                    .andWhere { AssetTable.collectionId eq collectionId.value }
                    .firstOrNull()?.toAsset()

    fun hasAssets(collectionId: CollectionId): Boolean =
            AssetTable
                    .select { AssetTable.collectionId eq collectionId.value }
                    .limit(1)
                    .toSet()
                    .isNotEmpty()

}