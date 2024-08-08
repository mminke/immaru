package com.earthrevealed.immaru.collections

interface CollectionRepository {
    suspend fun save(collection: Collection)
    suspend fun all(): List<Collection>
    suspend fun get(id: CollectionId): Collection?
    suspend fun delete(id: CollectionId)
}

class CollectionUpdateException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super("Could not update collection.", cause)
}