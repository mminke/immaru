package com.earthrevealed.immaru.collections

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CollectionDetailsViewModel(
    private val collectionRepository: CollectionRepository,
    collection: Collection
) : ViewModel() {
    val collection = mutableStateOf(collection)
    val errorMessage = mutableStateOf("")
    val state = mutableStateOf(State.READY)

    fun saveChanges() {
        viewModelScope.launch {
            state.value = State.SAVING
            try {
                collectionRepository.save(collection.value)
                state.value = State.READY
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Error occured while saving collection"
                state.value = State.ERROR
            }
        }
    }

    enum class State {
        READY,
        SAVING,
        ERROR
    }
}