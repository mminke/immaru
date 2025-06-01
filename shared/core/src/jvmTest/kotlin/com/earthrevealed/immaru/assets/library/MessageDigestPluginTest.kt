package com.earthrevealed.immaru.assets.library

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class MessageDigestPluginTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test encoding single byte array as input`() {
        val plugin = MessageDigestPlugin()
        val bytes = "MessageDigestPluginTest".toByteArray()

        plugin.processBytes(bytes)

        val expected = "c11f0b8332a6155176d9fdb286c15a4e86dd191e0b789d73d4dac51c62d17fd6"

        assertEquals(expected, plugin.result().toHexString())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test encoding multiple byte arrays as input`() {
        val plugin = MessageDigestPlugin()
        val bytes = "MessageDigestPluginTest".toByteArray()
        val bytes1 = bytes.sliceArray(0..10)
        val bytes2 = bytes.sliceArray(11..<bytes.size)

        plugin.processBytes(bytes1)
        plugin.processBytes(bytes2)

        val expected = "c11f0b8332a6155176d9fdb286c15a4e86dd191e0b789d73d4dac51c62d17fd6"

        assertEquals(expected, plugin.result().toHexString())
    }
}