package com.earthrevealed.immaru.collections

import com.earthrevealed.immaru.support.FixedClock
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
            createdAt = FixedClock.now()
        )

        assertNotNull(result)
        assertEquals(VALID_NAME, result.name)
        assertEquals(FixedClock.now(), result.createdAt)
    }

    @Test
    fun shouldFailToCreateCollectionForInvalidName() {
        assertFails {
            Collection(
                id = CollectionId(),
                name = INVALID_NAME,
                createdAt = FixedClock.now()
            )
        }
    }
}