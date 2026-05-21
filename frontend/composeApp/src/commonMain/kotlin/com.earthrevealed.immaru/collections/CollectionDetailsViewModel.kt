package com.earthrevealed.immaru.collections

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CollectionDetailsViewModel(
    private val collectionRepository: CollectionRepository,
    private val collectionId: CollectionId?,
) : ViewModel() {
    private val _collection = MutableStateFlow<Collection?>(null)
    val collection: StateFlow<Collection?> = _collection.asStateFlow()

    val errorMessage = mutableStateOf("")
    val state = mutableStateOf(State.READY)

    init {
        if (_collection.value == null && collectionId != null) {
            refreshCollection()
        } else {
            _collection.value = collection { }
        }
    }

    private fun refreshCollection() {
        viewModelScope.launch {
            state.value = State.PROCESSING
            try {
                val loadedCollection = collectionRepository.get(collectionId!!)
                if (loadedCollection == null) {
                    errorMessage.value = "Cannot retrieve collection!"
                    state.value = State.ERROR
                    return@launch
                }

                _collection.value = loadedCollection
                state.value = State.READY
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Cannot retrieve collection!"
                state.value = State.ERROR
            }
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING
            try {
                val currentCollection = collection.value ?: return@launch
                collectionRepository.save(currentCollection)
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
                val currentCollection = collection.value ?: return@launch
                collectionRepository.delete(currentCollection.id)
                state.value = State.READY
                onSuccess()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                errorMessage.value = "Error occured while deleting collection"
                state.value = State.ERROR
            }
        }
    }

    fun updateCollection(updatedCollection: Collection) {
        _collection.value = updatedCollection
        state.value = State.ISDIRTY
    }

    enum class State {
        READY,
        ISDIRTY,
        PROCESSING,
        ERROR
    }
}