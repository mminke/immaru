package com.earthrevealed.immaru.asset

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.collections.CollectionId
import kotlinx.coroutines.launch

class AssetDetailsViewModel(
    private val assetRepository: AssetRepository,
    private val collectionId: CollectionId,
    private val assetId: AssetId,
) : ViewModel() {
    val asset = mutableStateOf<Asset?>(null)
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf("")

    init {
        refreshAsset()
    }

    fun refreshAsset() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                asset.value = assetRepository.findById(collectionId, assetId)
                if (asset.value == null) {
                    errorMessage.value = "Cannot retrieve asset!"
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Cannot retrieve asset!"
            } finally {
                isLoading.value = false
            }
        }
    }
}

