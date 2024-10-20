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
import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.utils.io.pool.ByteArrayPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered

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
            }

            // Transfer the file
            if (false && file.supportsStreams()) {
                println("SUPPORTS STREAMS")
                val platformInputStream = file.getStream()
                val contentSource = object : RawSource {
                    override fun close() {
                        println("Closing source")
                        platformInputStream.close()
                    }

                    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
                        require(byteCount <= Int.MAX_VALUE)

                        println("READING AT MOST TO")
                        val test = awaitFor {
                            ByteArrayPool.useInstance { buffer ->
                                var copied = 0
                                val bufferSize = buffer.size.toLong()

                                with(DispatcherProvider.io()) {
                                    println("ENTERING WHILE LOOP")
                                    while (copied < byteCount) {
                                        println("COPIED $copied OF $byteCount")
                                        val rc =
                                            platformInputStream.readInto(buffer, byteCount.toInt())

                                        println("FINISHED READING INTO BUFFER")
                                        if (rc == -1) {
                                            if (copied == 0) copied = -1
                                            break
                                        }
                                        if (rc > 0) {
                                            sink.write(buffer.copyOf(rc))
                                            copied += rc
                                        }
                                    }
                                    println("COPIED $copied")

                                    copied
                                }
                            }
                        }
                        return test.toLong()

//                        return awaitFor {
//                            println("WAITING FOR RESULT")
//                            val result = test.await().toLong()
//                            println("WAITING FINISHED: $result")
//                            result
//                        }
                    }
                }

                println("START SAVING CONTENT")
                assetRepository.saveContentFor(newAsset, contentSource.buffered())

            } else {
                val buffer = Buffer().apply {
                    val content = file.readBytes()
                    write(content, 0, content.size)
               }
                assetRepository.saveContentFor(newAsset, buffer)
            }
        }

        refreshAssets()
    }
}

val FileAsset.contentUrl
    get() = "${Configuration.immaruUrl}/api/collections/${this.collectionId.value}/assets/${this.id.value}/content"
