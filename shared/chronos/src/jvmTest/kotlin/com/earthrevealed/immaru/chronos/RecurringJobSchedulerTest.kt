package com.earthrevealed.immaru.chronos

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RecurringJobSchedulerTest {

    @Test
    fun `scope overload - task runs repeatedly at interval`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            val job = scheduler.schedule(20.milliseconds, this) { runs++ }

            delay(90.milliseconds)
            job.cancelAndJoin()

            assertTrue(runs >= 3, "Expected at least 3 runs, got $runs")
        }
    }

    @Test
    fun `scope overload - rejects non-positive interval`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()

            assertFailsWith<IllegalArgumentException> {
                scheduler.schedule(0.milliseconds, this) { }
            }
        }
    }

    @Test
    fun `scope overload - task survives a failed iteration and keeps running`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            val job = scheduler.schedule(20.milliseconds, this) {
                runs++
                if (runs == 2) throw RuntimeException("simulated failure")
            }

            delay(90.milliseconds)
            job.cancelAndJoin()

            assertTrue(runs >= 3, "Expected task to recover from failure and keep running, got $runs runs")
        }
    }

    @Test
    fun `scope overload - cancelAll stops all scheduled jobs`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            scheduler.schedule(20.milliseconds, this) { runs++ }

            delay(60.milliseconds)
            scheduler.cancelAll()
            val snapshot = runs

            delay(60.milliseconds)

            assertTrue(snapshot > 0, "Expected at least one run before cancellation")
            assertTrue(runs == snapshot, "Expected no new runs after cancelAll, but count went from $snapshot to $runs")
        }
    }

    // --- Suspend overload ---

    @Test
    fun `suspend overload - task runs repeatedly at interval`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            val job = scheduler.schedule(20.milliseconds) { runs++ }

            delay(90.milliseconds)
            job.cancelAndJoin()

            assertTrue(runs >= 3, "Expected at least 3 runs, got $runs")
        }
    }

    @Test
    fun `suspend overload - rejects non-positive interval`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()

            assertFailsWith<IllegalArgumentException> {
                scheduler.schedule(0.milliseconds) { }
            }
        }
    }

    @Test
    fun `suspend overload - task survives a failed iteration and keeps running`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            val job = scheduler.schedule(20.milliseconds) {
                runs++
                if (runs == 2) throw RuntimeException("simulated failure")
            }

            delay(90.milliseconds)
            job.cancelAndJoin()

            assertTrue(runs >= 3, "Expected task to recover from failure, got $runs runs")
        }
    }

    @Test
    fun `suspend overload - job is cancelled when returned Job is cancelled`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            val job = scheduler.schedule(20.milliseconds) { runs++ }

            delay(60.milliseconds)
            job.cancelAndJoin()
            val snapshot = runs

            delay(60.milliseconds)

            assertTrue(snapshot > 0, "Expected some runs before cancel")
            assertFalse(runs > snapshot, "Expected no runs after cancel, but count went from $snapshot to $runs")
        }
    }

    @Test
    fun `suspend overload - cancelAll stops all scheduled jobs`() {
        runBlocking {
            val scheduler = RecurringJobScheduler()
            var runs = 0

            scheduler.schedule(20.milliseconds) { runs++ }

            delay(60.milliseconds)
            scheduler.cancelAll()
            val snapshot = runs

            delay(60.milliseconds)

            assertTrue(snapshot > 0, "Expected at least one run before cancellation")
            assertTrue(runs == snapshot, "Expected no new runs after cancelAll")
        }
    }

    @Test
    fun `cron scope overload executes once per matching minute`() {
        runBlocking {
            var runs = 0
            val clock = MutableClock(Instant.fromEpochSeconds(10 * 60L))
            val scheduler = RecurringJobScheduler(
                clock = clock,
            )

            val job = scheduler.schedule(
                cronExpression = "*/5 * * * *",
                scope = this,
                pollInterval = 10.milliseconds,
            ) {
                runs++
            }

            delay(40.milliseconds)
            assertEquals(1, runs)

            clock.currentInstant += 1.minutes
            delay(40.milliseconds)
            assertEquals(1, runs)

            clock.currentInstant += 4.minutes
            delay(40.milliseconds)
            assertEquals(2, runs)

            job.cancelAndJoin()
        }
    }

    @Test
    fun `cron suspend overload executes once per matching minute`() {
        runBlocking {
            var runs = 0
            val clock = MutableClock(Instant.fromEpochSeconds(0))
            val scheduler = RecurringJobScheduler(
                clock = clock,
            )

            val job = scheduler.schedule(
                cronExpression = "*/15 * * * *",
                pollInterval = 10.milliseconds,
            ) {
                runs++
            }

            delay(40.milliseconds)
            assertEquals(1, runs)

            clock.currentInstant += 14.minutes
            delay(40.milliseconds)
            assertEquals(1, runs)

            clock.currentInstant += 1.minutes
            delay(40.milliseconds)
            assertEquals(2, runs)

            job.cancelAndJoin()
        }
    }
}

private class MutableClock(var currentInstant: Instant) : Clock {
    override fun now(): Instant = currentInstant
}
