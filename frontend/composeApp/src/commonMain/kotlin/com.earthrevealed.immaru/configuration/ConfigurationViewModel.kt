package com.earthrevealed.immaru.configuration

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val configurationRepository: ConfigurationRepository,
    private val initialConfiguration: Configuration
) : ViewModel() {
    val serverUrl = mutableStateOf(initialConfiguration.serverUrl?:"")
    val state = mutableStateOf(State.READY)

    fun updateUrl(newUrl: String) {
        serverUrl.value = newUrl
        state.value = State.ISDIRTY
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING

            configurationRepository.update(
                initialConfiguration.copy(
                    serverUrl = serverUrl.value
                )
            )
            onSuccess()
        }
    }
}

enum class State {
    READY,
    ISDIRTY,
    PROCESSING,
    ERROR
}