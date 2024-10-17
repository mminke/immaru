package com.earthrevealed.immaru.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual object DispatcherProvider {
    actual fun io(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}

actual fun <T> awaitFor(suspendBlock: suspend CoroutineScope.() -> T): T {
    return runBlocking {
        suspendBlock()
    }
}