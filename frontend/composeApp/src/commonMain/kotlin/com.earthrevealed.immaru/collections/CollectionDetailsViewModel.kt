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

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING
            try {
                collectionRepository.save(collection.value)
                state.value = State.READY
                onSuccess()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Error occured while saving collection"
                state.value = State.ERROR
            }
        }
    }

    fun deleteCollection(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING
            try {
                collectionRepository.delete(collection.value.id)
                state.value = State.READY
                onSuccess()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Error occured while deleting collection"
                state.value = State.ERROR
            }
        }
    }

    enum class State {
        READY,
        PROCESSING,
        ERROR
    }
}