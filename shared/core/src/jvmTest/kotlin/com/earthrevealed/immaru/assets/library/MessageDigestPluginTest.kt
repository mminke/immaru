package com.earthrevealed.immaru.assets.library

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class MessageDigestPluginTest {
    private val testData = "MessageDigestPluginTest".toByteArray()
    private val testDataHash = "c11f0b8332a6155176d9fdb286c15a4e86dd191e0b789d73d4dac51c62d17fd6"

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test encoding single byte array as input`() {
        val plugin = MessageDigestPlugin()
        val bytes = testData

        runBlocking {
            plugin.prepare()
            plugin.processBytes(bytes)
            plugin.finish()

            assertEquals(testDataHash, plugin.result().toHexString())
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test encoding multiple byte arrays as input`() {
        val plugin = MessageDigestPlugin()
        val bytes1 = testData.sliceArray(0..10)
        val bytes2 = testData.sliceArray(11..<testData.size)

        runBlocking {
            plugin.prepare()
            plugin.processBytes(bytes1)
            plugin.processBytes(bytes2)
            plugin.finish()

            assertEquals(testDataHash, plugin.result().toHexString())
        }
    }
}