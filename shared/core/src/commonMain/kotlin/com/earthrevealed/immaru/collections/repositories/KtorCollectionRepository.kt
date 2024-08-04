package com.earthrevealed.immaru.collections.repositories

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorCollectionRepository(private val httpClient: HttpClient) : CollectionRepository {

    override suspend fun all(): List<Collection> {
        return try {
            httpClient.get("api/collections")
                .body<List<Collection>>()
        } catch (throwable: Throwable) {
            throw CollectionRetrievalException(throwable)
        }
    }

    override suspend fun insert(collection: Collection) {
        TODO("Not yet implemented")
    }

    override suspend fun update(collection: Collection) {
        try {
            httpClient.post("api/collections") {
                contentType(ContentType.Application.Json)
                setBody(collection)
            }.also {
                println("Update status code: ${it.status}")
            }
        } catch (throwable: Throwable) {
            throw CollectionRetrievalException(throwable)
        }
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