package com.earthrevealed.immaru.lightbox

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.AssetRetrievalException
import com.earthrevealed.immaru.collections.Collection
import kotlinx.coroutines.launch

class LightboxViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) :
    ViewModel() {
    val assets = mutableStateOf<List<Asset>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    init {
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
}