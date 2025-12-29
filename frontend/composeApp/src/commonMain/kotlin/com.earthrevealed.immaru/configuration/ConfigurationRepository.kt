package com.earthrevealed.immaru.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface ConfigurationRepository {
    suspend fun save(configuration: Configuration)

    val configuration: Flow<Configuration>
}

@Serializable
data class Configuration(
    val serverConfigurations: List<ServerConfiguration> = emptyList(),
    val activeServerConfigurationName: String? = null
) {
    val activeServerConfiguration = serverConfigurations.firstOrNull { it.name == activeServerConfigurationName }
}

@Serializable
data class ServerConfiguration(
    val name: String,
    val url: String
) {
    init {
        require(name.matches(Regex("[a-zA-Z0-9 _-]+"))) {
            "Name can only contain letters, number, spaces, underscores and hyphens."
        }
        require(url.matches(Regex("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"))) {
            "URL is not valid."
        }
    }
}