package com.earthrevealed.immaru.assets.repositories

import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.collections.CollectionId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.pool.ByteArrayPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered

class KtorAssetRepository(private val httpClient: HttpClient) : AssetRepository {
    override suspend fun findById(collectionId: CollectionId, assetId: AssetId): Asset? {
        return try {
            httpClient.get("api/collections") {
                url {
                    appendPathSegments(collectionId.value.toString())
                    appendPathSegments("assets")
                    appendPathSegments(assetId.value.toString())
                }
            }.body<Asset>()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

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

    override suspend fun save(asset: Asset) {
        TODO("Not yet implemented")
    }

    override suspend fun saveContentFor(asset: FileAsset, contentSource: Source) {
        TODO("Not yet implemented")
    }

    override suspend fun getContentFor(asset: FileAsset): Source {
        return try {
            httpClient.get("api/collections") {
                url {
                    appendPathSegments(asset.collectionId.value.toString())
                    appendPathSegments("assets")
                    appendPathSegments(asset.id.value.toString())
                    appendPathSegments("content")
                }
            }.bodyAsChannel().toSource()
        } catch (throwable: Throwable) {
            throw AssetRetrievalException(throwable)
        }
    }

    override suspend fun delete(id: AssetId) {
        TODO("Not yet implemented")
    }
}


class ByteReadChannelSource(
    private val byteReadChannel: ByteReadChannel
) : RawSource {
    override fun close() {
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0) { "Limit shouldn't be negative: $byteCount" }

        return ByteArrayPool.useInstance { buffer ->
            var copied = 0L
            val bufferSize = buffer.size.toLong()

            while (copied < byteCount) {
                val rc = runBlocking(Dispatchers.IO) {
                    byteReadChannel.readAvailable(
                        buffer,
                        0,
                        minOf(byteCount - copied, bufferSize).toInt()
                    )
                }
                if (rc == -1) {
                    if (copied == 0L) copied = -1
                    break
                }
                if (rc > 0) {
                    sink.write(buffer.copyOf(rc))
                    copied += rc
                }
            }

            copied
        }
    }
}

fun ByteReadChannel.toSource(): Source {
    return ByteReadChannelSource(this).buffered()
}