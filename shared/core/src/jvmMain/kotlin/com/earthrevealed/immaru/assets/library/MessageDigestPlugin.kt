package com.earthrevealed.immaru.assets.library

import java.security.MessageDigest

class MessageDigestPlugin {
    private val algorithm = "SHA-256"
    private val messageDigest = MessageDigest.getInstance(algorithm)

    fun processBytes(bytes: ByteArray) {
        messageDigest.update(bytes)
    }

    fun result(): ByteArray {
        return messageDigest.digest()
    }
}