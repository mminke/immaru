package com.earthrevealed.immaru.assets

import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface AssetRepository {
    suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset?
    suspend fun findAllFor(collectionId: CollectionId): List<Asset>
    suspend fun save(asset: Asset)
    suspend fun delete(id: AssetId)
    suspend fun getContentFor(asset: FileAsset): Flow<ByteArray>
    suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>)

    suspend fun findSelectableDates(collectionId: CollectionId): List<SelectableYear>
}

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