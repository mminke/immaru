package com.earthrevealed.immaru

import com.earthrevealed.immaru.common.HttpClientProvider
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import com.earthrevealed.immaru.coroutines.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ImmaruHttpClientProvider(configurationRepository: ConfigurationRepository) :
    HttpClientProvider {
    private var currentHttpClient: HttpClient? = null

    override val httpClient = configurationRepository
        .configuration
        .map { configuration ->
            currentHttpClient?.close()

            val activeServerUrl = configuration.activeServerConfiguration?.url

            activeServerUrl?.let { serverUrl ->
                HttpClient {
                    install(Logging) {
                        level = io.ktor.client.plugins.logging.LogLevel.NONE
                    }

                    install(ContentNegotiation) {
                        json()
                    }

                    install(Resources)

                    defaultRequest {
                        url(serverUrl)
                    }
                }.also {
                    currentHttpClient = it
                }
            }
        }.stateIn(CoroutineScope(DispatcherProvider.io()), SharingStarted.Eagerly, null)
}
