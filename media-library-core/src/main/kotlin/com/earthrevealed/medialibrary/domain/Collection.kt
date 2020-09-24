package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import com.earthrevealed.medialibrary.common.MediaLibraryBuilder
import java.time.OffsetDateTime
import java.util.*

data class CollectionId(
        val value: UUID = UUID.randomUUID()
)

data class Collection(
        val id: CollectionId,
        val name: String,
        val createdAt: OffsetDateTime
)

fun collection(initialization: CollectionBuilder.() -> Unit) =
        CollectionBuilder().apply(initialization).build()

@MediaLibraryBuilder
class CollectionBuilder {
    var id: CollectionId = CollectionId()
    var name: String = "Default"
    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)

    fun build() = Collection(
            id = id,
            name = name,
            createdAt = creationDateTime
    )
}