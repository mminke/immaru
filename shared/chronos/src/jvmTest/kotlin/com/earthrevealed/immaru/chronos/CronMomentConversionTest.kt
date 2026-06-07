package com.earthrevealed.immaru.chronos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class CronMomentConversionTest {

    @Test
    fun `fromInstant maps unix epoch correctly`() {
        val result = CronMoment.fromInstant(Instant.fromEpochSeconds(0))

        assertEquals(
            CronMoment(
                minute = 0,
                hour = 0,
                dayOfMonth = 1,
                month = 1,
                dayOfWeek = 4, // Thursday
            ),
            result,
        )
    }

    @Test
    fun `fromInstant maps sunday day-of-week to zero`() {
        // 1970-01-04T00:00:00Z is Sunday
        val result = CronMoment.fromInstant(Instant.fromEpochSeconds(3 * 86_400L))

        assertEquals(0, result.dayOfWeek)
    }
}
