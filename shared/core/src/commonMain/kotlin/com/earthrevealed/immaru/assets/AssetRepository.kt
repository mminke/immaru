package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId

interface AssetRepository {
    suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset?
    suspend fun findAllFor(collectionId: CollectionId): List<Asset>
    suspend fun save(asset: Asset)
    suspend fun delete(id: AssetId)
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