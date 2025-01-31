package com.earthrevealed.immaru.common.io

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.Source

fun ByteReadChannel.toFlow(): Flow<ByteArray> {
    return flow {
        val bufferSize = 2000
        val buffer = ByteArray(bufferSize)

        do {
            val rc = this@toFlow.readAvailable(buffer, 0, buffer.size)
            if (rc > 0) {
                emit(buffer.copyOf(rc))
            }
        } while (rc > -1)
    }
}


fun Source.toFlow(): Flow<ByteArray> {
    return flow {
        val bufferSize = 2000
        val buffer = ByteArray(bufferSize)

        do {
            val rc = this@toFlow.readAtMostTo(buffer,0, buffer.size)
            if(rc > 0) {
                emit(buffer.copyOf(rc))
            }
            if(rc == 0) {
                delay(1000)
            }
        } while (rc > -1)

        this@toFlow.close()
    }
}
