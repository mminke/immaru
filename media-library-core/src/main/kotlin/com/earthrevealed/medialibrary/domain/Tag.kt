package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import com.earthrevealed.medialibrary.common.MediaLibraryBuilder
import java.time.OffsetDateTime
import java.util.*

data class TagId(
        val value: UUID = UUID.randomUUID()
)

data class Tag(
        val id: TagId,
        val name: String,
        val createdAt: OffsetDateTime
)

fun tag(initialization: TagBuilder.() -> Unit) =
        TagBuilder().apply(initialization).build()

@MediaLibraryBuilder
class TagBuilder {
    var id: TagId = TagId()
    lateinit var name: String
    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)

    fun build() = Tag(
            id = id,
            name = name,
            createdAt = creationDateTime
    )
}