package com.earthrevealed.kotlin.io.ktor

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.pool.ByteArrayPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered

class ByteReadChannelSource(
    private val byteReadChannel: ByteReadChannel
) : RawSource {
    override fun close() {
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0) { "Limit shouldn't be negative: $byteCount" }

        return ByteArrayPool.useInstance { buffer ->
            var copied = 0L
            val bufferSize = buffer.size.toLong()

            while (copied < byteCount) {
                val rc = runBlocking(Dispatchers.IO) {
                    byteReadChannel.readAvailable(
                        buffer,
                        0,
                        minOf(byteCount - copied, bufferSize).toInt()
                    )
                }
                if (rc == -1) {
                    if (copied == 0L) copied = -1
                    break
                }
                if (rc > 0) {
                    sink.write(buffer.copyOf(rc))
                    copied += rc
                }
            }

            copied
        }
    }
}

fun ByteReadChannel.toSource(): Source {
    return ByteReadChannelSource(this).buffered()
}