package com.earthrevealed.immaru.common

import androidx.compose.runtime.mutableStateOf
import com.earthrevealed.immaru.assets.Category
import com.earthrevealed.immaru.lightbox.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoadableListContent(
    val contentLoader: suspend () -> List<Item<Category>>
) {
    private val _items = MutableStateFlow<List<Item<Category>>>(emptyList())
    val items = _items.asStateFlow()
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    suspend fun load() {
        isLoading.value = true
        try {
            _items.value = contentLoader()
        } catch (exception: Exception) {
            exception.printStackTrace()
            errorMessage.value = "Cannot load content."
        }
        isLoading.value = false
    }
}