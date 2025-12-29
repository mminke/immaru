package com.earthrevealed.immaru.configuration

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ServerConfigurationTest {

    companion object {
        private const val ALPHA_NUMERIC_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val VALID_SERVER_NAME_CHARACTERS = "$ALPHA_NUMERIC_CHARACTERS _-"
        private const val INVALID_SERVER_NAME_CHARACTERS = "!@#$%^&*()+=~`{}[]|\\:;'<>?,./"
    }

    @Test
    fun `valid server configuration`() {
        ServerConfiguration(VALID_SERVER_NAME_CHARACTERS, "https://example.com")
    }

    @Test
    fun `invalid server configuration name`() {
        INVALID_SERVER_NAME_CHARACTERS.forEach { char ->
            assertFailsWith<IllegalArgumentException>("Should fail for character: $char") {
                ServerConfiguration("invalid${char}name", "https://example.com")
            }
        }
    }

    @Test
    fun `invalid server configuration url`() {
        assertFailsWith<IllegalArgumentException> {
            ServerConfiguration("valid-name", "example")
        }
    }

    @Test
    fun `invalid protocol for server configuration url`() {
        assertFailsWith<IllegalArgumentException> {
            ServerConfiguration("valid-name", "ftp://example.com")
        }
    }

    @Test
    fun `url without protocol is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            ServerConfiguration("valid-name", "example.com")
        }
    }
}