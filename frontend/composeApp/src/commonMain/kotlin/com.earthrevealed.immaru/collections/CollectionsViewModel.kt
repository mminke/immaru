package com.earthrevealed.immaru.collections

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.collections.repositories.CollectionRetrievalException
import kotlinx.coroutines.launch

class CollectionsViewModel(private val collectionRepository: CollectionRepository) :
    ViewModel() {
    val collections = mutableStateOf<List<Collection>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            try {
                collections.value = collectionRepository.collections()
            } catch (exception: CollectionRetrievalException) {
                errorMessage.value = "Cannot retrieve collections!"
            }
            isLoading.value = false
        }
    }
}