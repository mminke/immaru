package com.earthrevealed.immaru.lightbox

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.common.io.toFlow
import com.earthrevealed.immaru.coroutines.DispatcherProvider
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LightboxViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) : ViewModel() {
    val assets = mutableStateOf<List<Asset>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)
    val showInformation = mutableStateOf(false)

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

    fun toggleShowInformation() {
        showInformation.value = !showInformation.value
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
                        val contentSource = file.openInputStream()?.toFlow()
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
            println("Uploading file: Use streaming")

            val contentSource = file.toKmpFile().openInputStream()?.toFlow()
                ?: throw IllegalStateException("Cannot open inputstream for file")

            assetRepository.saveContentFor(newAsset, contentSource)
        } else {
            println("Uploading file: Fallback to reading entire file into memory")

            val buffer = flow {
                emit(file.readBytes())
            }
            assetRepository.saveContentFor(newAsset, buffer)
        }
    }
}
