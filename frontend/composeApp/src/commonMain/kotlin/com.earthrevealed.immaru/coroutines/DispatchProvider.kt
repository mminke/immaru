package com.earthrevealed.immaru.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

expect object DispatcherProvider {
    fun io(): CoroutineDispatcher
}

expect fun <T> awaitFor(suspendBlock: suspend CoroutineScope.() -> T) : T