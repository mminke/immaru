package com.earthrevealed.immaru.assets

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MediaTypeSerializer::class)
data class MediaType(
    val type: String,
    val subtype: String,
    val parameters: Map<String, String> = emptyMap()
) {
    constructor(
        type: TYPE,
        subtype: String,
        parameters: Map<String, String> = emptyMap()
    ) : this(type.value, subtype, parameters)

    init {
        require(type.length <= MAX_TYPE_LENGTH) { "Type length exceeds maximum: ${MAX_TYPE_LENGTH}" }
        require(subtype.length <= MAX_SUBTYPE_LENGTH) { "Subtype length exceeds maximum: ${MAX_SUBTYPE_LENGTH}" }
    }

    override fun toString(): String {
        val base = "$type/$subtype"
        return if (parameters.isEmpty()) {
            base
        } else {
            "$base; ${parameters.entries.joinToString("; ") { "${it.key}=${it.value}" }}"
        }
    }

    companion object {
        fun parse(mediaTypeString: String): MediaType {
            require(mediaTypeString.isNotBlank()) { "No media type defined." }
            require(mediaTypeString.contains("/")) { "Not a valid media type." }

            val parts = mediaTypeString.split(";")
            val parameters = parts.drop(1).map {
                val pair = it.trim().split("=")
                pair[0] to pair[1]
            }.toMap()

            val typeDefinition = parts[0].split("/")

            return MediaType(typeDefinition[0], typeDefinition[1], parameters)
        }

        const val MAX_TYPE_LENGTH = 127
        const val MAX_SUBTYPE_LENGTH = 127

        val IMAGE_JPEG = MediaType(TYPE.IMAGE, "jpeg")
        val IMAGE_PNG = MediaType(TYPE.IMAGE, "png")
        val IMAGE_TIFF = MediaType(TYPE.IMAGE, "tiff")
        val IMAGE_GIF = MediaType(TYPE.IMAGE, "gif")
        val TEXT_PLAIN = MediaType(TYPE.TEXT, "plain")
    }

    enum class TYPE(val value: String) {
        APPLICATION("application"),
        AUDIO("audio"),
        EXAMPLE("example"),
        FONT("font"),
        HAPTICS("haptics"),
        IMAGE("image"),
        MESSAGE("message"),
        MODEL("model"),
        MULTIPART("multipart"),
        TEXT("text"),
        VIDEO("video"),
    }
}

object MediaTypeSerializer : KSerializer<MediaType?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MediaType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MediaType?) {
        if (value == null) return

        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): MediaType? {
        val value = decoder.decodeString()
        if (value == "null") return null
        return MediaType.parse(value)
    }
}