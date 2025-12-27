package com.earthrevealed.immaru.configuration

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface ConfigurationRepository {
    suspend fun save(configuration: Configuration)

    val configuration: Flow<Configuration>
}

@Serializable
data class ServerConfiguration(
    val name: String,
    val url: String
)

@Serializable
data class Configuration(
    val serverConfigurations: List<ServerConfiguration> = emptyList(),
    val useActiveConfiguration: String? = null
)