package com.earthrevealed.immaru.maintenance

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.library.Library
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.files.Path
import java.util.UUID

class MaintenanceService(
    private val library: Library,
    private val assetRepository: AssetRepository,
) {
    suspend fun findOrphanedFiles(): Flow<AssetId> = flow {
        library.browseAllFiles().collect { path ->
            val assetId = extractAssetId(path)
            if (assetId != null && !assetRepository.assetExists(assetId)) {
                emit(assetId)
            }
        }
    }

    private fun extractAssetId(path: Path): AssetId? {
        val filename = java.nio.file.Path.of(path.toString()).fileName.toString()
        val basename = filename.substringBeforeLast(".", filename)

        return try {
            AssetId.fromString(UUID.fromString(basename).toString())
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
