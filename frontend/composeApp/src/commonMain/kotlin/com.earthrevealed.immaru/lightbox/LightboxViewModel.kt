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
import com.earthrevealed.immaru.collections.CollectionId
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.common.io.kB
import com.earthrevealed.immaru.common.io.toFlow
import com.earthrevealed.immaru.coroutines.DispatcherProvider
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered

class LightboxViewModel(
    private val assetRepository: AssetRepository,
    private val collectionRepository: CollectionRepository,
    private val collectionId: CollectionId,
) : ViewModel() {
    val collection = mutableStateOf<Collection?>(null)
    val assets = mutableStateListOf<Asset>()
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    private val _selectedAssets = MutableStateFlow<List<Asset>>(emptyList())
    val selectedAssets = _selectedAssets.asStateFlow()

    init {
        refreshCollection()
    }

    private fun refreshCollection() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val loadedCollection = collectionRepository.get(collectionId)
                if (loadedCollection == null) {
                    errorMessage.value = "Cannot retrieve collection!"
                    return@launch
                }

                collection.value = loadedCollection
                refreshAssetsFor(loadedCollection)
            } catch (exception: Throwable) {
                exception.printStackTrace()
                errorMessage.value = "Cannot retrieve collection!"
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun refreshAssetsFor(currentCollection: Collection) {
        try {
            val retrievedAssets = assetRepository.findAllFor(currentCollection.id)
            assets.clear()
            assets.addAll(retrievedAssets)
        } catch (exception: AssetRetrievalException) {
            exception.printStackTrace()
            errorMessage.value = "Cannot retrieve assets!"
        }
    }

    fun refreshAssets() {
        viewModelScope.launch {
            val currentCollection = collection.value ?: return@launch
            isLoading.value = true
            try {
                refreshAssetsFor(currentCollection)
            } finally {
                isLoading.value = false
            }
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
        val currentCollection = collection.value ?: return
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

    fun createAssetsFor(directory: PlatformFile) {

        directory.list().forEach { file ->
            if (file.isRegularFile()) {
                println("Processing file: ${file.name}")
                if(file.size() <= 0) {
                    println("File ignored [length=${file.size()}]")
                    //TODO: Give feedback to user about this
                } else {
                    createAssetFor(file)
                }
            }
        }
    }

    private suspend fun transferFile(
        file: PlatformFile,
        newAsset: FileAsset
    ) {
        val contentSource = file.source().buffered().toFlow(32.kB)
            assetRepository.saveContentFor(newAsset, contentSource)
    }
}
