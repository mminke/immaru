package com.earthrevealed.immaru.collections

import com.earthrevealed.immaru.common.ImmaruBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
data class CollectionId(
    val value: Uuid = Uuid.random()
) {
    companion object {
        fun fromString(value: String) = CollectionId(
            Uuid.parse(value)
        )
    }
}

object CollectionIdSerializer : KSerializer<CollectionId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CollectionId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CollectionId) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): CollectionId {
        val value = decoder.decodeString()
        return CollectionId.fromString(value)
    }
}

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