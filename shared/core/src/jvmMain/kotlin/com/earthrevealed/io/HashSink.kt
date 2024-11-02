package com.earthrevealed.io

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import java.security.MessageDigest

class HashSink(algorithm: String = "SHA-256") : RawSink {
    private val tempBuffer = Buffer()
    private val messageDigest = MessageDigest.getInstance(algorithm)
    private var upstreamSink: RawSink? = null
    var hashValue: ByteArray? = null
        private set

    fun pipeTo(sink: RawSink) {
        upstreamSink = sink
    }

    override fun write(source: Buffer, byteCount: Long) {
        source.copyTo(tempBuffer, 0, byteCount)

        while (!tempBuffer.exhausted()) {
            messageDigest.update(tempBuffer.readByte())
        }

        upstreamSink?.write(source, byteCount)
    }

    override fun flush() {
        upstreamSink?.flush()
    }

    override fun close() {
        hashValue = messageDigest.digest()
        upstreamSink?.close()
    }
}