package com.earthrevealed.immaru.domain

import com.earthrevealed.immaru.common.ImmaruBuilder
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*

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
        var width: Width = Width.UNKNOWN,
        var height: Height = Height.UNKNOWN
) : Asset() {
    init {
        require(mediaType.isCompatible(MEDIATYPE_IMAGE))
    }
}

data class Video(
        override val id: AssetId,
        override val collectionId: CollectionId,
        override val originalFilename: String,
        override var originalCreatedAt: OriginalDateOfCreation? = null,
        override val mediaType: MediaType,
        override val tagIds: Set<TagId>,
        override val audit: Audit,
        var width: Width = Width.UNKNOWN,
        var height: Height = Height.UNKNOWN,
        var frameRate: FrameRate = FrameRate.UNKNOWN
): Asset() {
    init {
        require(mediaType.isCompatible(MEDIATYPE_VIDEO))
    }
}

abstract class TruncatedOffsetDateTime(value: OffsetDateTime) {
    val value: OffsetDateTime = value.truncatedTo(ChronoUnit.MILLIS)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TruncatedOffsetDateTime

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class OriginalDateOfCreation(value: OffsetDateTime): TruncatedOffsetDateTime(value) {
    companion object {
        fun of(instant: Instant) = OriginalDateOfCreation(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
        fun of(value: String): OriginalDateOfCreation {
            try {
                val metadataDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                return OriginalDateOfCreation(LocalDateTime.parse(value, metadataDateTimeFormatter).atZone(ZoneId.of("Europe/Paris")).toOffsetDateTime())
            } catch ( exception: DateTimeParseException ) {
                val metadataDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                return OriginalDateOfCreation(LocalDateTime.parse(value, metadataDateTimeFormatter).atZone(ZoneId.of("Europe/Paris")).toOffsetDateTime())
            }
        }
    }
}

class CreatedAt(value: OffsetDateTime): TruncatedOffsetDateTime(value) {
    companion object {
        fun now() = CreatedAt(OffsetDateTime.now())
        fun of(instant: Instant) = CreatedAt(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
    }
}

class LastModifiedAt(value: OffsetDateTime): TruncatedOffsetDateTime(value) {
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

data class Width constructor(val value: Pixel) {
    fun isUnknown() = value == -1.px

    companion object {
        val UNKNOWN = Width(-1.px)
        fun of(value: Pixel?) = if(value==null) UNKNOWN else Width(value)
        fun of(value: String) = Width(Pixel.of(value))
    }
}

data class Height constructor(val value: Pixel) {
    fun isUnknown() = value == -1.px

    companion object {
        val UNKNOWN = Height(-1.px)
        fun of(value: Pixel?) = if (value == null) UNKNOWN else Height(value)
        fun of(value: String) = Height(Pixel.of(value))
    }
}

data class FrameRate(val value: Int) {
    companion object {
        val UNKNOWN = FrameRate(-1)
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

fun video(collectionId: CollectionId, initialization: VideoBuilder.() -> Unit) =
        VideoBuilder(collectionId).apply(initialization).build()

@ImmaruBuilder
class ImageBuilder(val collectionId: CollectionId) {
    var id: AssetId = AssetId()
    lateinit var mediaType: MediaType
    lateinit var originalFilename: String
    var originalDateOfCreation: OriginalDateOfCreation? = null
    var width: Width = Width.UNKNOWN
    var height: Height = Height.UNKNOWN
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
            width = width,
            height = height,
            tagIds = tagIds,
            audit = auditBlock
    )
}

@ImmaruBuilder
class VideoBuilder(val collectionId: CollectionId) {
    var id: AssetId = AssetId()
    lateinit var mediaType: MediaType
    lateinit var originalFilename: String
    var originalDateOfCreation: OriginalDateOfCreation? = null
    var frameRate: FrameRate = FrameRate.UNKNOWN
    var width: Width = Width.UNKNOWN
    var height: Height = Height.UNKNOWN
    var tagIds = setOf<TagId>()
    var auditBlock: Audit = Audit()

    fun audit(initialization: AuditBuilder.() -> Unit) {
        auditBlock = AuditBuilder().apply(initialization).build()
    }

    fun build() = Video(
            id = id,
            collectionId = collectionId,
            mediaType = mediaType,
            originalFilename = originalFilename,
            originalCreatedAt = originalDateOfCreation,
            frameRate = frameRate,
            width = width,
            height = height,
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