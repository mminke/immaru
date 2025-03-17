package com.earthrevealed.immaru.lightbox

import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LightboxViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) : ViewModel() {
    val assets = mutableStateListOf<Asset>()
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    private val _selectedAssets = MutableStateFlow<List<Asset>>(emptyList())
    val selectedAssets = _selectedAssets.asStateFlow()

    init {
        refreshAssets()
    }

    private fun refreshAssets() {
        viewModelScope.launch {
            try {
                val retrievedAssets = assetRepository.findAllFor(currentCollection.id)
                assets.clear()
                assets.addAll(retrievedAssets)
            } catch (exception: AssetRetrievalException) {
                exception.printStackTrace()
                errorMessage.value = "Cannot retrieve assets!"
            }
            isLoading.value = false
        }
    }

    fun toggleAssetSelected(asset: Asset) {
        _selectedAssets.update {
            if(it.contains(asset)) {
                it - asset
            } else {
                it + asset
            }
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
                if(file.getLength() <= 0) {
                    println("File ignored [length=${file.getLength()}]")
                    //TODO: Give feedback to user about this
                } else {
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
