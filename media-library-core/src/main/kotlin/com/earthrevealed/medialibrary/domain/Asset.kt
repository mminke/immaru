package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import java.time.OffsetDateTime
import java.util.*

data class AssetId(
        val value: UUID = UUID.randomUUID()
)

data class Asset(
        val id: AssetId,
        val originalFilename: String,
        val createdAt: OffsetDateTime
)

fun asset(initialization: AssetBuilder.() -> Unit) =
        AssetBuilder().apply(initialization).build()

@DslMarker
annotation class MediaLibraryBuilder

@MediaLibraryBuilder
class AssetBuilder {
    var id: AssetId = AssetId()
    lateinit var originalFilename: String
    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)

    fun build() = Asset(
            id = id,
            originalFilename = originalFilename,
            createdAt = creationDateTime
    )
}