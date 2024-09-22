package com.earthrevealed.immaru.assets

import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaTypeTest {

    @Test
    fun `test parsing of full media type with parameters`() {
        val result =
            MediaType.parse("application/vnd.example; something=123abc; something-else=\"abcd\"")

        assertEquals("application", result.type)
        assertEquals("vnd.example", result.subtype)
        assertEquals(2, result.parameters.size)
        assertEquals("123abc", result.parameters["something"])
        assertEquals("\"abcd\"", result.parameters["something-else"])
    }

    @Test
    fun `test parsing of empty string returns exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            MediaType.parse("")
        }
    }

    @Test
    fun `test parsing invalid media type`() {
        assertThrows(IllegalArgumentException::class.java) {
            MediaType.parse("something")
        }
    }

    @Test
    fun `test parsing too long type`() {
        assertThrows(IllegalArgumentException::class.java) {
            val type = "A".repeat(MediaType.MAX_TYPE_LENGTH + 1)
            val subType = "subtype"
            MediaType.parse("$type/$subType")
        }
    }

    @Test
    fun `test parsing too long sub-type`() {
        assertThrows(IllegalArgumentException::class.java) {
            val type = "type"
            val subType = "A".repeat(MediaType.MAX_SUBTYPE_LENGTH + 1)
            MediaType.parse("$type/$subType")
        }
    }
}