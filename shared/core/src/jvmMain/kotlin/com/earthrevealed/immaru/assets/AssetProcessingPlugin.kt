package com.earthrevealed.immaru.assets

interface AssetProcessingPlugin {
    suspend fun prepare()
    suspend fun processBytes(bytes: ByteArray)
    suspend fun finish()
}