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

    fun updateUrl(newUrl: String) {
        serverUrl.value = newUrl
    }

    fun saveConfiguration() {
        viewModelScope.launch {
            configurationRepository.update(
                initialConfiguration.copy(
                    serverUrl = serverUrl.value
                )
            )
        }
    }
}