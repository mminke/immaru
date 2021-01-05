package com.earthrevealed.immaru.domain

import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.common.ImmaruBuilder
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.*

data class AssetId(
        val value: UUID = UUID.randomUUID()
)

data class Asset(
        val id: AssetId,
        val collectionId: CollectionId,
        val originalFilename: String,
        val createdAt: OffsetDateTime,
        val tagIds: Set<TagId>
) {

    fun internalFilename() =
            "${id.value.toString()}.${extension()}"

    fun internalFilelocation(): Path = destinationFolders()
            .resolve(internalFilename())

    /**
     * Determine the destination folder to store the asset in.
     * Sub folders are determined by the first 6 digits of the UUID:
     * id = a4e6d238-39eb-4efc-b23d-be6ac0f05e75
     * destination folder = /a4/e6/d2/38/
     */
    fun destinationFolders(): Path {
        var subFolders = Path.of("")
        (0..3).forEach {
            val offset = (it * 2)
            subFolders = subFolders.resolve(id.value.toString().substring(offset + 0..offset + 1))
        }
        return subFolders
    }

    fun extension(): String? {
        val index = originalFilename.lastIndexOf('.')
        if (index > 0) {
            return originalFilename.substring(index + 1)
        }
        return null
    }
}

fun asset(collectionId: CollectionId, initialization: AssetBuilder.() -> Unit) =
        AssetBuilder(collectionId).apply(initialization).build()

@ImmaruBuilder
class AssetBuilder(val collectionId: CollectionId) {
    var id: AssetId = AssetId()
    lateinit var originalFilename: String
    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)
    var tagIds = setOf<TagId>()

    fun build() = Asset(
            id = id,
            collectionId = collectionId,
            originalFilename = originalFilename,
            createdAt = creationDateTime,
            tagIds = tagIds
    )
}