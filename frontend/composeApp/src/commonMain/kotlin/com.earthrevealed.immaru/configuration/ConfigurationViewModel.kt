package com.earthrevealed.immaru.configuration

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val configurationRepository: ConfigurationRepository,
    initialConfiguration: Configuration
) : ViewModel() {
    val serverConfigurations = mutableStateOf(initialConfiguration.serverConfigurations)
    val activeServerConfigurationName = mutableStateOf(initialConfiguration.activeServerConfigurationName)

    val state = mutableStateOf(State.READY)
    val nameError = mutableStateOf<String?>(null)
    val urlError = mutableStateOf<String?>(null)

    fun setConfiguration(configuration: Configuration) {
        serverConfigurations.value = configuration.serverConfigurations
        activeServerConfigurationName.value = configuration.activeServerConfigurationName
        state.value = State.READY
    }

    fun addConfiguration(name: String, url: String): Boolean {
        return try {
            serverConfigurations.value = serverConfigurations.value + ServerConfiguration(name, url)
            state.value = State.IS_DIRTY
            clearValidationErrors()
            true
        } catch (e: IllegalArgumentException) {
            handleValidationError(e)
            false
        }
    }

    fun updateConfiguration(originalName: String, name: String, url: String): Boolean {
        return try {
            val updatedConfiguration = ServerConfiguration(name, url)
            serverConfigurations.value = serverConfigurations.value.map {
                if (it.name == originalName) updatedConfiguration else it
            }
            if (activeServerConfigurationName.value == originalName) {
                activeServerConfigurationName.value = name
            }
            state.value = State.IS_DIRTY
            clearValidationErrors()
            true
        } catch (e: IllegalArgumentException) {
            handleValidationError(e)
            false
        }
    }

    fun clearValidationErrors() {
        nameError.value = null
        urlError.value = null
    }

    private fun handleValidationError(e: IllegalArgumentException) {
        if (e.message?.contains("Name", ignoreCase = true) == true) {
            nameError.value = e.message
            urlError.value = null
        } else {
            urlError.value = e.message
            nameError.value = null
        }
    }

    fun removeConfiguration(name: String) {
        serverConfigurations.value = serverConfigurations.value.filterNot { it.name == name }
        if (activeServerConfigurationName.value == name) {
            activeServerConfigurationName.value = null
        }
        state.value = State.IS_DIRTY
    }

    fun setActiveConfiguration(name: String) {
        activeServerConfigurationName.value = name
        state.value = State.IS_DIRTY
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING

            configurationRepository.save(
                Configuration(
                    serverConfigurations = serverConfigurations.value,
                    activeServerConfigurationName = activeServerConfigurationName.value
                )
            )
            onSuccess()
        }
    }
}

enum class State {
    READY,
    IS_DIRTY,
    PROCESSING,
    ERROR
}
