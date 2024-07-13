package com.earthrevealed.immaru.collections.repositories

import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.collection
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

class KtorCollectionRepository(private val httpClient: HttpClient) : CollectionRepository {

    override suspend fun all(): List<Collection> {
        return try {
            httpClient.get("api/collections")
                .body<List<Collection>>()
        } catch (throwable: Throwable) {
            throw CollectionRetrievalException(throwable)
        }
    }

    override suspend fun save(collection: Collection) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: CollectionId): Collection? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(collection: Collection) {
        TODO("Not yet implemented")
    }
}

class CollectionRetrievalException(throwable: Throwable) :
    RuntimeException("Cannot retrieve collections.", throwable)