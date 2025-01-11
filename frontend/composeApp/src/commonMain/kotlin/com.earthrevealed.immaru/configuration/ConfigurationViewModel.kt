package com.earthrevealed.immaru.configuration

import DataStoreConfigurationRepository
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val configurationRepository: DataStoreConfigurationRepository,
    initialServerUrl: String
) : ViewModel() {
    val serverUrl = mutableStateOf(initialServerUrl)

    fun updateUrl(newUrl: String) {
        serverUrl.value = newUrl
    }

    fun saveConfiguration() {
        viewModelScope.launch {
            configurationRepository.savePreference(
                "immaru.server.url",
                serverUrl.value
            )
        }
    }
}