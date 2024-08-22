package com.earthrevealed.immaru.assets.repositories

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.collections.CollectionId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

class KtorAssetRepository(private val httpClient: HttpClient) : AssetRepository {

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return try {
            httpClient.get("api/collections") {
                url {
                    appendPathSegments(collectionId.value.toString())
                    appendPathSegments("assets")
                }
            }.body<List<Asset>>()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }
}
