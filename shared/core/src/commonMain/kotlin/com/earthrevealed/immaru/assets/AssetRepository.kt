package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId

interface AssetRepository {
    suspend fun findAllFor(collectionId: CollectionId): List<Asset>
}

class AssetRetrievalException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot retrieve assets.", throwable)
}