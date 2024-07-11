package com.earthrevealed.immaru.collections

interface CollectionRepository {
    suspend fun save(collection: Collection)
    suspend fun all(): List<Collection>
    suspend fun get(id: CollectionId): Collection?
    suspend fun delete(collection: Collection)
}