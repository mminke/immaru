package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.AuditFields
import com.earthrevealed.immaru.common.GenericId
import com.earthrevealed.immaru.common.GenericIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

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
        return if(other is Asset) {
            this.id == other.id
        } else {
            false
        }
    }
}

@Serializable
@SerialName("FileAsset")
class FileAsset : Asset {
    val originalFilename: String

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

    val mediaTypeIsNotDefined: Boolean
        get() = mediaType == null

    constructor(
        collectionId: CollectionId,
        originalFilename: String,
    ) : super(collectionId = collectionId, name = originalFilename) {
        this.name = originalFilename
        this.originalFilename = originalFilename
    }

    // This constructor is needed to recreate an existing asset from persistence
    internal constructor(
        id: AssetId,
        collectionId: CollectionId,
        name: String,
        mediaType: MediaType?,
        originalFilename: String,
        contentHash: ByteArray?,
        auditFields: AuditFields,
    ) : super(
        id = id,
        collectionId = collectionId,
        name = name,
        auditFields = auditFields
    ) {
        this.originalFilename = originalFilename
        this.mediaType = mediaType
        this.contentHash = contentHash
    }

    fun registerContentDetails(mediaType: MediaType, hash: ByteArray) {
        this.mediaType = mediaType
        this.contentHash = hash

        this.touch()
    }

    fun changeName(name: String) {
        this.name = name

        this.touch()
    }

    override fun equals(other: Any?) = when (other) {
        is FileAsset -> this.id == other.id
        else -> false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Serializable(with = AssetIdSerializer::class)
class AssetId(value: Uuid = Uuid.random()): GenericId(value) {
    companion object {
         fun fromString(value: String) = AssetId(Uuid.parse(value))
    }
}

object AssetIdSerializer : GenericIdSerializer<AssetId>({ value -> AssetId.fromString(value)})
