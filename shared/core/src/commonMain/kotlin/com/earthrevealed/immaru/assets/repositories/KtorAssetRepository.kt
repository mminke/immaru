package com.earthrevealed.immaru.assets.repositories

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetCursor
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetPage
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.AssetStatus
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.assets.PageDirection
import com.earthrevealed.immaru.assets.RetrievalException
import com.earthrevealed.immaru.assets.SaveAssetException
import com.earthrevealed.immaru.assets.SelectableYear
import com.earthrevealed.immaru.assets.api.Collections
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.common.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.put
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
    private fun apiHtpClient() = httpClientProvider.httpClient.value?.config {
        defaultRequest {
            url {
                appendPathSegments("api/")
            }
        }
    }

    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? {
        return try {
            apiHtpClient()?.get(
                Collections.ById.Assets.ById(
                    parent = Collections.ById.Assets(
                        collection = Collections.ById(id1 = collectionId)
                    ),
                    id2 = assetId
                )
            )?.body<Asset>()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

    override suspend fun findAllFor(collectionId: CollectionId, status: AssetStatus?): List<Asset> {
        return try {
            apiHtpClient()?.get(
                Collections.ById.Assets(
                    collection = Collections.ById(id1 = collectionId),
                    status = status?.name
                )
            )?.body<List<Asset>>()
                ?: emptyList()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

    override suspend fun save(asset: Asset) {
        try {
            val httpResponse = apiHtpClient()
                ?.put(
                    Collections.ById.Assets(
                        collection = Collections.ById(id1 = asset.collectionId)
                    )
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(asset)
                }

            if (httpResponse?.status?.isSuccess() != true) {
                throw SaveAssetException("Something went wrong saving the asset [status=${httpResponse?.status}]")
            }
        } catch (throwable: Throwable) {
            throw SaveAssetException(throwable)
        }
    }

    override suspend fun saveContentFor(asset: FileAsset, content: Flow<ByteArray>) {
        try {
            val httpResponse = apiHtpClient()
                ?.put(
                    Collections.ById.Assets.ById.Content(
                        asset = Collections.ById.Assets.ById(
                            parent = Collections.ById.Assets(
                                collection = Collections.ById(id1 = asset.collectionId)
                            ),
                            id2 = asset.id
                        )
                    )
                ) {
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
        TODO("Not yet implemented (content provided by direct url reference)")
//        return try {
//            httpClient()!!
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

    override suspend fun assetExists(assetId: AssetId): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: AssetId) {
        TODO("Not yet implemented")
    }

    override suspend fun findSelectableDates(collectionId: CollectionId): List<SelectableYear> {
        return try {
            apiHtpClient()
                ?.get(
                    Collections.ById.Selectors(
                        collection = Collections.ById(id1 = collectionId)
                    )
                )?.body<List<SelectableYear>>()
                ?: emptyList()
        } catch (throwable: Throwable) {
            throw RetrievalException(SelectableYear::class, throwable)
        }
    }

    override suspend fun findPageFor(
        collectionId: CollectionId,
        limit: Int,
        cursor: AssetCursor?,
        direction: PageDirection,
        status: AssetStatus?
    ): AssetPage {
        return try {
            apiHtpClient()
                ?.get(
                    Collections.ById.Assets(
                        collection = Collections.ById(id1 = collectionId),
                        limit = limit,
                        status = status?.name,
                        direction = direction.name,
                        cursorOriginalCreatedAt = cursor?.originalCreatedAt?.toString(),
                        cursorId = cursor?.id?.toString()
                    )
                )?.body<AssetPage>()
                ?: AssetPage(items = emptyList(), nextCursor = null, prevCursor = null, hasMore = false)
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }
}
