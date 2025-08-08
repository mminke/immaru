package com.earthrevealed.immaru.collections

import com.earthrevealed.immaru.common.GenericId
import com.earthrevealed.immaru.common.GenericIdSerializer
import com.earthrevealed.immaru.common.ImmaruBuilder
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Collection(
    val id: CollectionId,
    val name: String,
    val createdAt: Instant
) {
    init {
        require(name.length <= 100) { "Collection name should not exceed a length of 100 characters" }
    }
}

@Serializable(with = CollectionIdSerializer::class)
class CollectionId(value: Uuid = Uuid.random()): GenericId(value) {
    companion object {
        fun fromString(value: String) = CollectionId(Uuid.parse(value))
    }
}

object CollectionIdSerializer : GenericIdSerializer<CollectionId>({ value -> CollectionId.fromString(value)})

fun collection(initialization: CollectionBuilder.() -> Unit) =
    CollectionBuilder().apply(initialization).build()

@ImmaruBuilder
class CollectionBuilder {
    var id = CollectionId()
    var name = "New collection"
    var createdAt: Instant = Clock.System.now()

    fun build() = Collection(
        id = id,
        name = name,
        createdAt = createdAt
    )
}