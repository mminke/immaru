package com.earthrevealed.immaru.assets

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.AuditFields
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
}

@Serializable
@SerialName("FileAsset")
class FileAsset : Asset {
    val originalFilename: String
    val originalCreatedOn: Instant

    var mediaType: MediaType? = null
        private set(value) {
            check(field == null) { "Media type can only be set once" }
            field = value
        }

    val mediaTypeIsNotDefined: Boolean
        get() = mediaType == null

    constructor(
        collectionId: CollectionId,
        originalFilename: String,
        originalCreatedOn: Instant
    ) : super(collectionId = collectionId, name = originalFilename) {
        this.name = originalFilename
        this.originalFilename = originalFilename
        this.originalCreatedOn = originalCreatedOn
    }

    // This constructor is needed to recreate an existing asset from persistence
    internal constructor(
        id: AssetId,
        collectionId: CollectionId,
        name: String,
        mediaType: MediaType?,
        originalFilename: String,
        originalCreatedOn: Instant,
        auditFields: AuditFields,
    ) : super(
        id = id,
        collectionId = collectionId,
        name = name,
        auditFields = auditFields
    ) {
        this.originalFilename = originalFilename
        this.originalCreatedOn = originalCreatedOn
        this.mediaType = mediaType
    }

    class FileAssetUpdater(var name: String, var mediaType: MediaType?)

    fun update(collectChanges: FileAssetUpdater.() -> Unit) {
        val updater = FileAssetUpdater(this.name, this.mediaType)
        updater.collectChanges()

        this.name = updater.name
        this.mediaType = updater.mediaType

        this.auditFields.registerModification()
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
