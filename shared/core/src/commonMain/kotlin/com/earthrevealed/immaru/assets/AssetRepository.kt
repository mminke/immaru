package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.io.Sink
import kotlinx.io.Source

interface AssetRepository {
    suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset?
    suspend fun findAllFor(collectionId: CollectionId): List<Asset>
    suspend fun save(asset: Asset)
    suspend fun delete(id: AssetId)
    fun getContentFor(asset: FileAsset): Sink
    suspend fun saveContentFor(asset: FileAsset, contentSource: Source)
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