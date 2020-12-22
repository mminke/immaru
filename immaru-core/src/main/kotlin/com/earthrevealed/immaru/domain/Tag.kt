package com.earthrevealed.immaru.domain

import com.earthrevealed.immaru.common.ClockProvider
import com.earthrevealed.immaru.common.ImmaruBuilder
import java.time.OffsetDateTime
import java.util.*

data class TagId(
        val value: UUID = UUID.randomUUID()
) {
    constructor(value: String): this(UUID.fromString(value))
}

data class Tag(
        val id: TagId,
        val collectionId: CollectionId,
        val name: String,
        val createdAt: OffsetDateTime
)

fun tag(collectionId: CollectionId, initialization: TagBuilder.() -> Unit) =
        TagBuilder(collectionId).apply(initialization).build()

@ImmaruBuilder
class TagBuilder(val collectionId: CollectionId) {
    var id: TagId = TagId()
    lateinit var name: String
    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)

    fun build() = Tag(
            id = id,
            collectionId = collectionId,
            name = name,
            createdAt = creationDateTime
    )
}