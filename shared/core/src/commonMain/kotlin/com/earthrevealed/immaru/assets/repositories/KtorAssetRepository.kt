package com.earthrevealed.immaru.assets.repositories

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.RetrievalException
import com.earthrevealed.immaru.assets.SaveAssetException
import com.earthrevealed.immaru.assets.Year
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.content.ChannelWriterContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.flow.Flow

class KtorAssetRepository(
    private val httpClientProvider: HttpClientProvider
) : AssetRepository {
    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? {
        return try {
            httpClientProvider.httpClient.value
                ?.get("api/collections") {
                    url {
                        appendPathSegments(collectionId.value.toString())
                        appendPathSegments("assets")
                        appendPathSegments(assetId.value.toString())
                    }
                }?.body<Asset>()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

    override suspend fun findAllFor(collectionId: CollectionId): List<Asset> {
        return try {
            httpClientProvider.httpClient.value
                ?.get("api/collections") {
                    url {
                        appendPathSegments(collectionId.value.toString())
                        appendPathSegments("assets")
                    }
                }?.body<List<Asset>>()
                ?: emptyList()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

    override suspend fun save(asset: Asset) {
        try {
            httpClientProvider.httpClient.value
                ?.put("api/collections") {
                    url {
                        appendPathSegments(asset.collectionId.value.toString())
                        appendPathSegments("assets")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(asset)
                }
        } catch (throwable: Throwable) {
            throw SaveAssetException(throwable)
        }
    }

    override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        try {
            val httpResponse = httpClientProvider.httpClient.value
                ?.put("api/collections") {
                    url {
                        appendPathSegments(asset.collectionId.value.toString())
                        appendPathSegments("assets")
                        appendPathSegments(asset.id.value.toString())
                        appendPathSegments("content")
                    }
                    setBody(
                        ChannelWriterContent(
                            {
                                content.collect { buffer ->
                                    writeFully(buffer, 0, buffer.size)
                                }
                            },
                            ContentType.Application.OctetStream
                        )
                    )
                }

            if (httpResponse?.status?.isSuccess() != true) {
                throw SaveAssetException("Something went wrong saving the content [status=${httpResponse?.status}]")
            }
        } catch (throwable: Throwable) {
            throw SaveAssetException(throwable)
        }
    }

    override suspend fun getContentFor(asset: FileAsset): Flow<ByteArray> {
        TODO("Not yet implemented")
//        return try {
//            httpClientProvider.httpClient.value!!
//                .get("api/collections") {
//                    url {
//                        appendPathSegments(asset.collectionId.value.toString())
//                        appendPathSegments("assets")
//                        appendPathSegments(asset.id.value.toString())
//                        appendPathSegments("content")
//                    }
//                }.bodyAsChannel().toFlow()
//        } catch (throwable: Throwable) {
//            throw AssetRetrievalException(throwable)
//        }
    }

    override suspend fun delete(id: AssetId) {
        TODO("Not yet implemented")
    }

    override suspend fun findAvailableDateSelectors(collectionId: CollectionId): List<Year> {
        return try {
            httpClientProvider.httpClient.value
                ?.get("api/collections") {
                    url {
                        appendPathSegments(collectionId.value.toString())
                        appendPathSegments("available-date-selectors")
                    }
                }?.body<List<Year>>()
                ?: emptyList()
        } catch (throwable: Throwable) {
            throw RetrievalException(Year::class, throwable)
        }
    }
}


