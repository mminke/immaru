package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.AuditFields
import com.earthrevealed.immaru.common.GenericId
import com.earthrevealed.immaru.common.GenericIdSerializer
import com.earthrevealed.immaru.common.truncatedToMicroseconds
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class AssetStatus {
    CREATED,
    CONTENT_READY,
    ORPHANED_FILE,
    ORPHANED,
    DELETED,
}

@Serializable
sealed class Asset {
    val id: AssetId
    val collectionId: CollectionId
    val auditFields: AuditFields
    var name: String
        protected set

    constructor(
        collectionId: CollectionId,
        name: String
    ) {
        this.id = AssetId()
        this.collectionId = collectionId
        this.name = name
        this.auditFields = AuditFields()
    }

    // This constructor is needed to recreate an existing asset from persistence
    constructor(
        id: AssetId,
        collectionId: CollectionId,
        name: String,
        auditFields: AuditFields,
    ) {
        this.id = id
        this.collectionId = collectionId
        this.name = name
        this.auditFields = auditFields
    }

    protected fun touch() {
        this.auditFields.registerModification()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Asset) {
            this.id == other.id &&
                    this.collectionId.equals(other.collectionId) &&
                    this.name == other.name &&
                    this.auditFields == other.auditFields
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + collectionId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + auditFields.hashCode()
        return result
    }
}

@Serializable
@SerialName("FileAsset")
class FileAsset : Asset {
    val originalFilename: String
    var originalCreatedAt: Instant
        private set

    var mediaType: MediaType? = null
        private set(value) {
            check(field == null) { "Media type can only be set once" }
            field = value
        }

    var contentHash: ByteArray? = null
        private set(value) {
            check(field == null) { "Hash can only be set once" }
            field = value
        }

    var status: AssetStatus = AssetStatus.CREATED
        private set

    val mediaTypeIsNotDefined: Boolean
        get() = mediaType == null

    constructor(
        collectionId: CollectionId,
        originalFilename: String,
    ) : super(collectionId = collectionId, name = originalFilename) {
        this.name = originalFilename
        this.originalFilename = originalFilename
        this.originalCreatedAt = auditFields.createdAt
    }

    // This constructor is needed to recreate an existing asset from persistence
    internal constructor(
        id: AssetId,
        collectionId: CollectionId,
        name: String,
        mediaType: MediaType?,
        status: AssetStatus,
        originalFilename: String,
        originalCreatedAt: Instant,
        contentHash: ByteArray?,
        auditFields: AuditFields,
    ) : super(
        id = id,
        collectionId = collectionId,
        name = name,
        auditFields = auditFields
    ) {
        this.originalFilename = originalFilename
        this.originalCreatedAt = originalCreatedAt
        this.mediaType = mediaType
        this.contentHash = contentHash
        this.status = status
    }

    fun registerContentDetails(mediaType: MediaType, hash: ByteArray) {
        this.mediaType = mediaType
        this.contentHash = hash
        this.status = AssetStatus.CONTENT_READY

        this.touch()
    }

    fun registerOriginalCreatedAt(originalCreatedAt: Instant) {
        this.originalCreatedAt = originalCreatedAt.truncatedToMicroseconds()
    }

    fun changeName(name: String) {
        this.name = name

        this.touch()
    }

    override fun equals(other: Any?) = when (other) {
        is FileAsset -> {
            super.equals(other) &&
                    this.originalCreatedAt.equals(other.originalCreatedAt) &&
                    this.originalFilename == other.originalFilename &&
                    this.mediaType == other.mediaType &&
                    this.contentHash.contentEquals(other.contentHash) &&
                    this.status == other.status
        }

        else -> false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + originalCreatedAt.hashCode()
        result = 31 * result + originalFilename.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (contentHash?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        return result
    }
}

@Serializable(with = AssetIdSerializer::class)
class AssetId(value: Uuid = Uuid.random()) : GenericId(value) {
    companion object {
        fun fromString(value: String) = AssetId(Uuid.parse(value))
    }
}

object AssetIdSerializer : GenericIdSerializer<AssetId>({ value -> AssetId.fromString(value) })
