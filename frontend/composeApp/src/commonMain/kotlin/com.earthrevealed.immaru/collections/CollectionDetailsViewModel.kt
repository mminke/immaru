package com.earthrevealed.immaru.collections

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollectionDetailsViewModel(
    collection: Collection = collection { },
    private val collectionRepository: CollectionRepository
) : ViewModel() {
    val collection = mutableStateOf(collection)
    val errorMessage = mutableStateOf("")
    val state = mutableStateOf(State.READY)

    fun saveChanges() {
        viewModelScope.launch {
            state.value = State.SAVING
            try {
                delay(1000)
                collectionRepository.update(collection.value)
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