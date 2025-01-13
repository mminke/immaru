package com.earthrevealed.immaru.configuration

import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    suspend fun update(configuration: Configuration)

    val configuration: Flow<Configuration>
}

data class Configuration(
    val serverUrl: String? = null
)

