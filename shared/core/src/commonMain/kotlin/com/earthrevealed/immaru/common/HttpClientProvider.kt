package com.earthrevealed.immaru.common

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.StateFlow

interface HttpClientProvider {
    val httpClient: StateFlow<HttpClient?>
}