package com.earthrevealed.immaru.configuration

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val configurationRepository: ConfigurationRepository,
    initialConfiguration: Configuration
) : ViewModel() {
    val configurations = mutableStateOf(initialConfiguration.serverConfigurations)
    val activeConfiguration = mutableStateOf(initialConfiguration.useActiveConfiguration)

    val state = mutableStateOf(State.READY)

    fun setConfiguration(configuration: Configuration) {
        configurations.value = configuration.serverConfigurations
        activeConfiguration.value = configuration.useActiveConfiguration
        state.value = State.READY
    }

    fun addConfiguration(name: String, url: String) {
        configurations.value = configurations.value + ServerConfiguration(name, url)
        state.value = State.IS_DIRTY
    }

    fun updateConfiguration(originalName: String, name: String, url: String) {
        val originalConfiguration = configurations.value.find { it.name == originalName }
        if (originalConfiguration != null) {
            val updatedConfiguration = originalConfiguration.copy(name = name, url = url)
            configurations.value = configurations.value.map {
                if (it.name == originalName) updatedConfiguration else it
            }
            if (activeConfiguration.value == originalName) {
                activeConfiguration.value = name
            }
            state.value = State.IS_DIRTY
        }
    }

    fun removeConfiguration(name: String) {
        configurations.value = configurations.value.filterNot { it.name == name }
        if (activeConfiguration.value == name) {
            activeConfiguration.value = null
        }
        state.value = State.IS_DIRTY
    }

    fun setActiveConfiguration(name: String) {
        activeConfiguration.value = name
        state.value = State.IS_DIRTY
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state.value = State.PROCESSING

            configurationRepository.save(
                Configuration(
                    serverConfigurations = configurations.value,
                    useActiveConfiguration = activeConfiguration.value
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
