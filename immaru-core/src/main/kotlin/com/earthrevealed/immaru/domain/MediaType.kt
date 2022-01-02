package com.earthrevealed.immaru.domain

data class MediaType(
        val type: Type,
        val subtype: String? = null
) {

    companion object {
        fun valueOf(value: String): MediaType {
            require(value.isNotBlank())

            val parts = value.split("/", limit = 2)
            val type = Type.from(parts[0])
            val subType = if(parts.size == 2) { parts[1].let { if(it != "*") it else null } } else null

            return MediaType(type, subType)
        }
    }

    fun isCompatible(other: MediaType): Boolean {
        return this.type == other.type
    }

    override fun toString(): String {
        return "${type.value}/${subtype?:"*"}"
    }

    enum class Type(
            val value: String
    ) {
        APPLICATION("application"),
        AUDIO("audio"),
        IMAGE("image"),
        MESSAGE("message"),
        MULTIPART("multipart"),
        TEXT("text"),
        VIDEO("video");

        companion object {
            fun from(value: String) = values().first { it.value == value }
        }
    }
}

val MEDIATYPE_IMAGE = MediaType.valueOf("image/*")
val MEDIATYPE_VIDEO = MediaType.valueOf("video/*")
val MEDIATYPE_IMAGE_JPEG = MediaType.valueOf("image/jpeg")
val MEDIATYPE_IMAGE_PNG = MediaType.valueOf("image/png")
val MEDIATYPE_IMAGE_GIF = MediaType.valueOf("image/gif")
