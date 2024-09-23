package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset?
    suspend fun findAllFor(collectionId: CollectionId): List<Asset>
    suspend fun save(asset: Asset)
    suspend fun delete(id: AssetId)
    suspend fun saveContentFor(asset: FileAsset, toFlow: Flow<ByteArray>)
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