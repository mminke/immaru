package com.earthrevealed.immaru.collections

interface CollectionRepository {
    suspend fun save(collection: Collection)
    suspend fun all(): List<Collection>
    suspend fun get(id: CollectionId): Collection?
    suspend fun delete(id: CollectionId)
}

class CollectionRetrievalException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot retrieve collections.", throwable)
}

class SaveCollectionException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot save collection.", throwable)
    constructor(message: String) : super(message)
}

class DeleteCollectionException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot delete collection.", throwable)
    constructor(message: String) : super(message)
}