package com.earthrevealed.immaru.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

@OptIn(DelicateCoroutinesApi::class)
actual object DispatcherProvider {
    actual fun io(): CoroutineDispatcher {
        return newFixedThreadPoolContext(nThreads = 200, name = "IO")
    }
}

actual fun <T> awaitFor(suspendBlock: suspend CoroutineScope.() -> T): T {
    return runBlocking {
        suspendBlock()
    }
}