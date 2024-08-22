package com.earthrevealed.immaru.assets

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Asset(
    val id: AssetId,
    val collectionId: CollectionId,
//    val mediaType: MediaType,
    val originalFilename: String,
//    val originalCreatedAt: OriginalDateOfCreation?,
//    val tagIds: Set<TagId>,
//    val audit: Audit,
)

@Serializable(with = AssetIdSerializer::class)
data class AssetId(
    val value: Uuid = uuid4()
) {
    companion object {
        fun fromString(value: String) = AssetId(
            uuidFrom(value)
        )
    }
}

object AssetIdSerializer : KSerializer<AssetId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AssetId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AssetId) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): AssetId {
        val value = decoder.decodeString()
        return AssetId.fromString(value)
    }
}