package com.earthrevealed.immaru.maintenance

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetProcessingPlugin
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.assets.library.DetectMediaTypePlugin
import com.earthrevealed.immaru.assets.library.DetectOriginalCreatedAtPlugin
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.library.MessageDigestPlugin
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.AuditFields
import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.common.io.kB
import com.earthrevealed.immaru.common.io.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import kotlin.time.Instant

private val logger = KotlinLogging.logger { }

class MaintenanceService(
    private val library: Library,
    private val assetRepository: AssetRepository,
) {
    private val orphanedAssetName = "orphaned"

    fun createAssetsForOrphanedFiles(collectionId: CollectionId): Flow<AssetId> = flow {
        findOrphanedFiles().collect { orphanedFilePath ->
            logger.info { "Processing orphaned file: $orphanedFilePath" }

            val assetId = extractAssetId(orphanedFilePath) ?: return@collect
            val metadata = extractMetadataFrom(orphanedFilePath)
            val now = ClockProvider.clock.now()

            val orphanedAsset = FileAsset(
                id = assetId,
                collectionId = collectionId,
                name = orphanedAssetName,
                mediaType = metadata.mediaType,
                status = AssetStatus.ORPHANED_FILE,
                originalFilename = orphanedAssetName,
                originalCreatedAt = metadata.originalCreatedAt ?: fileCreatedAt(orphanedFilePath),
                contentHash = metadata.contentHash,
                auditFields = AuditFields(createdAt = now, lastModifiedAt = now),
            )

            assetRepository.save(orphanedAsset)
            emit(assetId)
        }
    }

    private fun findOrphanedFiles(): Flow<Path> = library.browseAllFiles()
        .filter { path ->
            extractAssetId(path)?.let { assetId -> !assetRepository.assetExists(assetId) } ?: false
        }

    private suspend fun extractMetadataFrom(path: Path): OrphanedMetadata {
        val mediaTypePlugin = DetectMediaTypePlugin()
        val messageDigestPlugin = MessageDigestPlugin()
        val originalCreatedAtPlugin = DetectOriginalCreatedAtPlugin()
        val plugins = listOf<AssetProcessingPlugin>(
            mediaTypePlugin,
            messageDigestPlugin,
            originalCreatedAtPlugin,
        )

        try {
            plugins.forEach { it.prepare() }

            SystemFileSystem.source(path).buffered().toFlow(32.kB).collect { chunk ->
                plugins.forEach { it.processBytes(chunk) }
            }
        } finally {
            plugins.forEach {
                runCatching { it.finish() }
            }
        }

        return OrphanedMetadata(
            mediaType = runCatching { mediaTypePlugin.result() }.getOrNull(),
            originalCreatedAt = originalCreatedAtPlugin.result(),
            contentHash = messageDigestPlugin.result(),
        )
    }

    private fun fileCreatedAt(path: Path): Instant {
        val attributes = Files.readAttributes(
            java.nio.file.Path.of(path.toString()),
            BasicFileAttributes::class.java,
        )
        val createdAt = attributes.creationTime().toInstant()
        val fallbackLastModifiedAt = attributes.lastModifiedTime().toInstant()
        val chosenTimestamp = if (createdAt.epochSecond > 0) createdAt else fallbackLastModifiedAt
        return Instant.parse(chosenTimestamp.toString())
    }

    private fun filename(path: Path): String =
        java.nio.file.Path.of(path.toString()).fileName.toString()

    private fun extractAssetId(path: Path): AssetId? {
        val filename = filename(path)
        val basename = filename.substringBeforeLast(".", filename)

        return try {
            AssetId.fromString(UUID.fromString(basename).toString())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private class OrphanedMetadata(
        val mediaType: MediaType?,
        val originalCreatedAt: Instant?,
        val contentHash: ByteArray,
    )
}
