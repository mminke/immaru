package com.earthrevealed.immaru.chronos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CronExpressionTest {

    @Test
    fun `cron field spec supports step syntax like star slash 5`() {
        val parsed = CronFieldSpec.parse("*/5")
        val resolved = parsed.resolve(0..59)

        assertEquals(setOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55), resolved)
    }

    @Test
    fun `cron field spec supports range step syntax`() {
        val parsed = CronFieldSpec.parse("1-10/3")
        val resolved = parsed.resolve(0..59)

        assertEquals(setOf(1, 4, 7, 10), resolved)
    }

    @Test
    fun `cron field spec supports lists`() {
        val parsed = CronFieldSpec.parse("1,5,9")
        val resolved = parsed.resolve(0..59)

        assertEquals(setOf(1, 5, 9), resolved)
    }

    @Test
    fun `cron expression matching works for step expressions`() {
        val expression = CronExpression.parse("*/5 * * * *")

        assertTrue(expression.matches(CronMoment(minute = 10, hour = 2, dayOfMonth = 1, month = 1, dayOfWeek = 1)))
        assertFalse(expression.matches(CronMoment(minute = 11, hour = 2, dayOfMonth = 1, month = 1, dayOfWeek = 1)))
    }

    @Test
    fun `cron expression parser rejects wrong number of fields`() {
        assertFailsWith<IllegalArgumentException> {
            CronExpression.parse("*/5 * * *")
        }
    }

    @Test
    fun `cron field spec rejects zero step`() {
        assertFailsWith<IllegalArgumentException> {
            CronFieldSpec.parse("*/0").resolve(0..59)
        }
    }
}
