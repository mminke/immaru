package com.earthrevealed.immaru.chronos

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * A simple recurring task scheduler.
 *
 * Usage:
 * ```kotlin
 * val scheduler = RecurringJobScheduler()
 * scheduler.schedule(Duration.parse("1h"), scope) {
 *     // task code here
 * }
 * ```
 */
class RecurringJobScheduler(
    private val clock: Clock = Clock.System,
) {
    private val jobs = mutableListOf<Job>()

    /**
     * Schedule a recurring job to run at fixed intervals.
     *
     * @param interval The duration between each invocation
     * @param scope The coroutine scope to launch the job in
     * @param task The lambda to execute repeatedly
     * @return The Job handle (can be cancelled to stop the recurring task)
     */
    fun schedule(
        interval: Duration,
        scope: CoroutineScope,
        task: suspend () -> Unit
    ): Job {
        require(interval > ZERO) { "interval must be greater than 0" }

        val job = scope.launchRecurringTask(interval, task)
        jobs.add(job)
        return job
    }

    /**
     * Schedule a recurring job from a suspending context.
     * The job is launched as a child of the caller's coroutine context,
     * so it respects structured concurrency and is cancelled when the parent scope is cancelled.
     *
     * @param interval The duration between each invocation
     * @param task The lambda to execute repeatedly
     * @return The Job handle (can be cancelled to stop the recurring task)
     */
    suspend fun schedule(
        interval: Duration,
        task: suspend () -> Unit
    ): Job {
        require(interval > ZERO) { "interval must be greater than 0" }

        val job = CoroutineScope(currentCoroutineContext()).launchRecurringTask(interval, task)
        jobs.add(job)
        return job
    }

    /**
     * Schedule a recurring job from a cron expression.
     *
     * The expression must use 5 fields: minute hour day-of-month month day-of-week.
     * This function polls the scheduler's clock with [pollInterval] and triggers [task] when the
     * expression matches. Each matching minute is executed once.
     */
    fun schedule(
        cronExpression: String,
        scope: CoroutineScope,
        pollInterval: Duration = Duration.parse("30s"),
        task: suspend () -> Unit,
    ): Job {
        require(pollInterval > ZERO) { "pollInterval must be greater than 0" }

        val parsed = CronExpression.parse(cronExpression)
        val job = scope.launchCronTask(parsed, nowProvider(), pollInterval, task)
        jobs.add(job)
        return job
    }

    /**
     * Suspend convenience overload for cron scheduling.
     */
    suspend fun schedule(
        cronExpression: String,
        pollInterval: Duration = Duration.parse("30s"),
        task: suspend () -> Unit,
    ): Job {
        require(pollInterval > ZERO) { "pollInterval must be greater than 0" }

        val parsed = CronExpression.parse(cronExpression)
        val job = CoroutineScope(currentCoroutineContext()).launchCronTask(parsed, nowProvider(), pollInterval, task)
        jobs.add(job)
        return job
    }

    /**
     * Cancel all scheduled jobs.
     */
    fun cancelAll() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun nowProvider(): () -> CronMoment = {
        CronMoment.fromInstant(clock.now())
    }
}

/**
 * Launch a recurring coroutine task that repeats at fixed intervals.
 *
 * @param interval Time between task invocations
 * @param task The suspend function to invoke repeatedly
 * @return The Job that can be cancelled to stop the recurrence
 */
fun CoroutineScope.launchRecurringTask(
    interval: Duration,
    task: suspend () -> Unit
): Job = launch {
    while (true) {
        try {
            task()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Keep recurring execution alive even when a task iteration fails.
        }
        delay(interval)
    }
}

private fun CoroutineScope.launchCronTask(
    expression: CronExpression,
    nowProvider: () -> CronMoment,
    pollInterval: Duration,
    task: suspend () -> Unit,
): Job = launch {
    var lastExecutedMoment: CronMoment? = null

    while (true) {
        val current = nowProvider()

        if (expression.matches(current) && current != lastExecutedMoment) {
            try {
                task()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // Keep recurring execution alive even when a cron iteration fails.
            }
            lastExecutedMoment = current
        }

        delay(pollInterval)
    }
}
