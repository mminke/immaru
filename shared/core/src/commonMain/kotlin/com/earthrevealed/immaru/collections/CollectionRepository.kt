package com.earthrevealed.immaru.collections

interface CollectionRepository {
    suspend fun update(collection: Collection)
    suspend fun all(): List<Collection>
    suspend fun get(id: CollectionId): Collection?
    suspend fun delete(collection: Collection)
    suspend fun insert(collection: Collection)
}

class CollectionUpdateException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super("Could not update collection.", cause)
}