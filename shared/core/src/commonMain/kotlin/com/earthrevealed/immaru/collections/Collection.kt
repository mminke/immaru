package com.earthrevealed.immaru.collections

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
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
    val value: Uuid = uuid4()
) {
    companion object {
        fun fromString(value: String) = CollectionId(
            uuidFrom(value)
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