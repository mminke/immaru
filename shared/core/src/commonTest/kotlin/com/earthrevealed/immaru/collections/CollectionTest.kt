package com.earthrevealed.immaru.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull


class CollectionTest {
    private val VALID_NAME = (1..100).map { "a" }.joinToString("")
    private val INVALID_NAME = (1..101).map { "a" }.joinToString("")

    @Test
    fun shouldCreateCollection() {
        val result = Collection(
            id = CollectionId(),
            name = VALID_NAME,
            createdAt = ""
        )

        assertNotNull(result)
        assertEquals(VALID_NAME, result.name)
    }

    @Test
    fun shouldFailToCreateCollectionForInvalidInput() {
        assertFails {
            Collection(
                id = CollectionId(),
                name = INVALID_NAME,
                createdAt = ""
            )
        }
    }
}