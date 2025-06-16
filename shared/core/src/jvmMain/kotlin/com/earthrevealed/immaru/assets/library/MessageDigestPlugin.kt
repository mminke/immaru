package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.AssetProcessingPlugin
import java.security.MessageDigest

class MessageDigestPlugin: AssetProcessingPlugin {
    private val algorithm = "SHA-256"
    private val messageDigest = MessageDigest.getInstance(algorithm)
    private lateinit var hashValue: ByteArray

    override suspend fun prepare() {
        // Nothing to prepare
    }

    override suspend fun processBytes(bytes: ByteArray) {
        messageDigest.update(bytes)
    }

    override suspend fun finish() {
        hashValue = messageDigest.digest()
    }

    fun result(): ByteArray {
        return hashValue
    }
}
