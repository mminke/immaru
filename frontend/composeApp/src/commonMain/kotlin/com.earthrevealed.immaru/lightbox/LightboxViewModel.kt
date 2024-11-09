package com.earthrevealed.immaru.lightbox

import Configuration
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.coroutines.DispatcherProvider
import com.earthrevealed.immaru.coroutines.awaitFor
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PlatformInputStream
import io.ktor.utils.io.pool.ByteArrayPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlin.math.min

class LightboxViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) : ViewModel() {
    val assets = mutableStateOf<List<Asset>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    init {
        refreshAssets()
    }

    private fun refreshAssets() {
        viewModelScope.launch {
            try {
                assets.value = assetRepository.findAllFor(currentCollection.id)
            } catch (exception: AssetRetrievalException) {
                exception.printStackTrace()
                errorMessage.value = "Cannot retrieve assets!"
            }
            isLoading.value = false
        }
    }

    fun createAssetFor(file: PlatformFile) {
        val newAsset = FileAsset(
            currentCollection.id,
            file.name,
        )
        viewModelScope.launch {
            withContext(DispatcherProvider.io()) {
                assetRepository.save(newAsset)

                transferFile(file, newAsset)
            }

            refreshAssets()
        }
    }

    fun createAssetsFor(directory: PlatformDirectory) {
        val kmpDirectory = directory.toKmpFile()

        kmpDirectory.listFiles()?.forEach { file ->
            if (file.isFile()) {
                println("Processing file: ${file.getName()}")

                val newAsset = FileAsset(
                    currentCollection.id,
                    file.getName(),
                )
                viewModelScope.launch {
                    withContext(DispatcherProvider.io()) {
                        assetRepository.save(newAsset)

                        // Transfer the file
                        val contentSource = file.openInputStream()
                            ?: throw IllegalStateException("Cannot open inputstream for file")
                        assetRepository.saveContentFor(newAsset, contentSource)
                    }

                    refreshAssets()
                }
            }
        }
    }

    private suspend fun transferFile(
        file: PlatformFile,
        newAsset: FileAsset
    ) {
        if (file.supportsStreams()) {
            println("Supports streaming")
            val platformInputStream = file.getStream()
            val contentSource = PlatformInputStreamSource(platformInputStream)

            assetRepository.saveContentFor(newAsset, contentSource.buffered())
        } else {
            println("Fallback to reading entire file into memory")
            val buffer = Buffer().apply {
                val content = file.readBytes()
                write(content, 0, content.size)
            }
            assetRepository.saveContentFor(newAsset, buffer)
        }
    }
}

val FileAsset.contentUrl
    get() = "${Configuration.immaruUrl}/api/collections/${this.collectionId.value}/assets/${this.id.value}/content"


class PlatformInputStreamSource(
    private val platformInputStream: PlatformInputStream
) : RawSource {
    override fun close() {
        platformInputStream.close()
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount <= Int.MAX_VALUE)

        val numberOfBytesCopied = awaitFor {
            ByteArrayPool.useInstance { buffer ->
                var copied = 0
                val bufferSize = buffer.size

                withContext(DispatcherProvider.io()) {
                    while (copied < byteCount) {
                        val rc =
                            platformInputStream.readInto(buffer, min(byteCount.toInt(), bufferSize))

                        if (rc == -1) {
                            if (copied == 0) copied = -1
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
        return numberOfBytesCopied.toLong()
    }
}