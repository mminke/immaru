package com.earthrevealed.immaru.domain

import com.earthrevealed.immaru.common.ImmaruBuilder
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.ws.rs.core.MediaType

val MEDIATYPE_IMAGE = MediaType.valueOf("image/*")
val MEDIATYPE_VIDEO = MediaType.valueOf( "video/*")
val MEDIATYPE_IMAGE_JPEG = MediaType.valueOf("image/jpeg")
val MEDIATYPE_IMAGE_PNG = MediaType.valueOf("image/png")
val MEDIATYPE_IMAGE_GIF = MediaType.valueOf("image/gif")

data class AssetId(
        val value: UUID = UUID.randomUUID()
)

sealed class Asset {
    abstract val id: AssetId
    abstract val collectionId: CollectionId
    abstract val mediaType: MediaType
    abstract val originalFilename: String
    abstract val originalCreatedAt: OriginalDateOfCreation?
    abstract val tagIds: Set<TagId>
    abstract val audit: Audit

    fun internalFilename() =
            "${id.value.toString()}.${extension()}"

    fun internalFilelocation(): Path = destinationFolder()
            .resolve(internalFilename())

    /**
     * Determine the destination folder to store the asset in.
     * Sub folders are determined by the first 6 digits of the UUID:
     * id = a4e6d238-39eb-4efc-b23d-be6ac0f05e75
     * destination folder = /a4/e6/d2/38/
     */
    fun destinationFolder(): Path {
        var subFolders = Path.of("")
        (0..3).forEach {
            val offset = (it * 2)
            subFolders = subFolders.resolve(id.value.toString().substring(offset + 0..offset + 1))
        }
        return subFolders
    }

    fun extension(): String? {
        val index = originalFilename.lastIndexOf('.')
        if (index > 0) {
            return originalFilename.substring(index + 1)
        }
        return null
    }
}

data class Image(
        override val id: AssetId,
        override val collectionId: CollectionId,
        override val originalFilename: String,
        override var originalCreatedAt: OriginalDateOfCreation? = null,
        override val mediaType: MediaType,
        override val tagIds: Set<TagId>,
        override val audit: Audit,
        var imageWidth: ImageWidth = ImageWidth.UNKNOWN,
        var imageHeight: ImageHeight = ImageHeight.UNKNOWN
) : Asset() {
    init {
        require(mediaType.isCompatible(MEDIATYPE_IMAGE))
    }
}

private val metadataDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

data class OriginalDateOfCreation(val value: OffsetDateTime) {
    companion object {
        fun of(instant: Instant) = OriginalDateOfCreation(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
        fun of(value: String) = OriginalDateOfCreation(LocalDateTime.parse(value, metadataDateTimeFormatter).atZone(ZoneId.of("Europe/Paris")).toOffsetDateTime())
    }
}

data class CreatedAt(val value: OffsetDateTime) {
    companion object {
        fun now() = CreatedAt(OffsetDateTime.now())
        fun of(instant: Instant) = CreatedAt(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
    }
}

data class LastModifiedAt(val value: OffsetDateTime) {
    companion object {
        fun now() = LastModifiedAt(OffsetDateTime.now())
        fun of(instant: Instant) = LastModifiedAt(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
    }
}

data class Pixel(val value: Int) {
    operator fun unaryMinus(): Pixel = Pixel(-this.value)

    companion object {
        fun of(value: String): Pixel = Pixel(value.removeSuffix(" pixels").toInt())
    }
}

val Int.px: Pixel
    get() = Pixel(this)

data class ImageWidth constructor(val value: Pixel) {
    fun isUnknown() = value == -1.px

    companion object {
        val UNKNOWN = ImageWidth(-1.px)
        fun of(value: Pixel?) = if(value==null) ImageWidth.UNKNOWN else ImageWidth(value)
        fun of(value: String) = ImageWidth(Pixel.of(value))
    }
}

data class ImageHeight constructor(val value: Pixel) {
    fun isUnknown() = value == -1.px

    companion object {
        val UNKNOWN = ImageHeight(-1.px)
        fun of(value: Pixel?) = if (value == null) ImageHeight.UNKNOWN else ImageHeight(value)
        fun of(value: String) = ImageHeight(Pixel.of(value))
    }
}

data class Audit(
        val createdAt: CreatedAt,
        var lastModifiedAt: LastModifiedAt
) {
    constructor() : this(CreatedAt.now(), LastModifiedAt.now())
    constructor(createdAt: CreatedAt) : this(createdAt, LastModifiedAt(createdAt.value))
}

fun image(collectionId: CollectionId, initialization: ImageBuilder.() -> Unit) =
        ImageBuilder(collectionId).apply(initialization).build()

@ImmaruBuilder
class ImageBuilder(val collectionId: CollectionId) {
    var id: AssetId = AssetId()
    lateinit var mediaType: MediaType
    lateinit var originalFilename: String
    var originalDateOfCreation: OriginalDateOfCreation? = null
    var imageWidth: ImageWidth = ImageWidth.UNKNOWN
    var imageHeight: ImageHeight = ImageHeight.UNKNOWN
    var tagIds = setOf<TagId>()
    var auditBlock: Audit = Audit()

    fun audit(initialization: AuditBuilder.() -> Unit) {
        auditBlock = AuditBuilder().apply(initialization).build()
    }

    fun build() = Image(
            id = id,
            collectionId = collectionId,
            mediaType = mediaType,
            originalFilename = originalFilename,
            originalCreatedAt = originalDateOfCreation,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            tagIds = tagIds,
            audit = auditBlock
    )
}

@ImmaruBuilder
class AuditBuilder() {
    lateinit var createdAt: CreatedAt
    lateinit var lastModifiedAt: LastModifiedAt

    fun build() = Audit(
            createdAt = createdAt,
            lastModifiedAt = lastModifiedAt
    )
}