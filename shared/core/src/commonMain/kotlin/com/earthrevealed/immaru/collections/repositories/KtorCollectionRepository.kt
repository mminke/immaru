package com.earthrevealed.immaru.collections.repositories

import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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

    override suspend fun save(collection: Collection) {
        val httpResponse = try {
            httpClient.put("api/collections") {
                contentType(ContentType.Application.Json)
                setBody(collection)
            }
        } catch (throwable: Throwable) {
            throw SaveCollectionException(throwable)
        }

        if (httpResponse.status != HttpStatusCode.Accepted) {
            throw SaveCollectionException("Could not save collection. [code=${httpResponse.status}]")
        }
    }

    override suspend fun get(id: CollectionId): Collection? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(collection: Collection) {
        TODO("Not yet implemented")
    }
}

class CollectionRetrievalException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot retrieve collections.", throwable)
}

class SaveCollectionException : RuntimeException {
    constructor(throwable: Throwable) : super("Cannot retrieve collections.", throwable)
    constructor(message: String) : super(message)
}