package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.time.Instant

interface AssetRepository {
    suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset?
    suspend fun findAllFor(collectionId: CollectionId, status: AssetStatus? = AssetStatus.CONTENT_READY): List<Asset>
    suspend fun save(asset: Asset)
    suspend fun delete(id: AssetId)
    suspend fun assetExists(assetId: AssetId): Boolean
    suspend fun getContentFor(asset: FileAsset): Flow<ByteArray>
    suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>)

    suspend fun findSelectableDates(collectionId: CollectionId): List<SelectableYear>
    suspend fun findPageFor(
        collectionId: CollectionId,
        limit: Int,
        cursor: AssetCursor?,
        direction: PageDirection,
        status: AssetStatus? = AssetStatus.CONTENT_READY
    ): AssetPage
}

@Serializable
data class AssetCursor(
    val originalCreatedAt: Instant,
    val id: AssetId,
)

@Serializable
enum class PageDirection {
    FORWARD,
    BACKWARD,
}

@Serializable
data class AssetPage(
    val items: List<Asset>,
    val nextCursor: AssetCursor?,
    val prevCursor: AssetCursor?,
    val hasMore: Boolean,
)

class RetrievalException : RuntimeException {
    constructor(clazz: KClass<out Any>, throwable: Throwable) : super("Cannot retrieve ${clazz.simpleName}.", throwable)
}

class AssetRetrievalException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot retrieve assets.", throwable)
}

class SaveAssetException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot save asset.", throwable)
    constructor(message: String) : super(message)
}

class DeleteAssetException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot delete asset.", throwable)
    constructor(message: String) : super(message)
}
