package com.earthrevealed.immaru.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual object DispatcherProvider {
    actual fun io(): CoroutineDispatcher {
        return Dispatchers.Default
    }
}

actual fun <T> awaitFor(suspendBlock: suspend CoroutineScope.() -> T): T {
    TODO("Not sure what to use as a replacement for runBlocking(..)")
}