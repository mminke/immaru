package com.earthrevealed.immaru.chronos

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant


/**
 * A minimal cron implementation supporting 5-part expressions:
 * minute hour day-of-month month day-of-week
 *
 * Supported syntax per field:
 * - *
 * - 5
 * - 1-10
 * - star-slash step (written as `* /5` without the space)
 * - 1-10/2
 * - 1,2,7
 */
data class CronExpression(
    val minute: CronFieldSpec,
    val hour: CronFieldSpec,
    val dayOfMonth: CronFieldSpec,
    val month: CronFieldSpec,
    val dayOfWeek: CronFieldSpec,
) {
    fun matches(moment: CronMoment): Boolean {
        return minute.matches(moment.minute, MINUTE_RANGE) &&
                hour.matches(moment.hour, HOUR_RANGE) &&
                dayOfMonth.matches(moment.dayOfMonth, DAY_OF_MONTH_RANGE) &&
                month.matches(moment.month, MONTH_RANGE) &&
                dayOfWeek.matches(moment.dayOfWeek, DAY_OF_WEEK_RANGE)
    }

    companion object {
        private val MINUTE_RANGE = 0..59
        private val HOUR_RANGE = 0..23
        private val DAY_OF_MONTH_RANGE = 1..31
        private val MONTH_RANGE = 1..12
        private val DAY_OF_WEEK_RANGE = 0..6

        fun parse(expression: String): CronExpression {
            val parts = expression.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            require(parts.size == 5) {
                "Cron expression must have exactly 5 fields (minute hour day-of-month month day-of-week)."
            }

            return CronExpression(
                minute = CronFieldSpec.parse(parts[0]),
                hour = CronFieldSpec.parse(parts[1]),
                dayOfMonth = CronFieldSpec.parse(parts[2]),
                month = CronFieldSpec.parse(parts[3]),
                dayOfWeek = CronFieldSpec.parse(parts[4]),
            )
        }
    }
}

data class CronMoment(
    val minute: Int,
    val hour: Int,
    val dayOfMonth: Int,
    val month: Int,
    val dayOfWeek: Int,
) {
    companion object {
        fun fromInstant(instant: Instant): CronMoment {
            val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
            return CronMoment(
                minute = localDateTime.minute,
                hour = localDateTime.hour,
                dayOfMonth = localDateTime.day,
                month = localDateTime.month.ordinal + 1,
                dayOfWeek = (localDateTime.dayOfWeek.ordinal + 1) % 7,
            )
        }
    }
}

sealed interface CronFieldSpec {
    fun matches(value: Int, bounds: IntRange): Boolean = value in resolve(bounds)

    fun resolve(bounds: IntRange): Set<Int>

    data object Any : CronFieldSpec {
        override fun resolve(bounds: IntRange): Set<Int> = bounds.toSet()
    }

    data class Single(val value: Int) : CronFieldSpec {
        override fun resolve(bounds: IntRange): Set<Int> {
            require(value in bounds) { "Value $value is out of bounds $bounds" }
            return setOf(value)
        }
    }

    data class Range(val start: Int, val end: Int) : CronFieldSpec {
        override fun resolve(bounds: IntRange): Set<Int> {
            require(start <= end) { "Invalid range $start-$end" }
            require(start in bounds && end in bounds) { "Range $start-$end is out of bounds $bounds" }
            return (start..end).toSet()
        }
    }

    data class Step(val base: CronFieldSpec, val step: Int) : CronFieldSpec {
        override fun resolve(bounds: IntRange): Set<Int> {
            require(step > 0) { "Step value must be greater than 0" }

            val baseValues = base.resolve(bounds).toList().sorted()
            require(baseValues.isNotEmpty()) { "Step base did not resolve to any values" }

            val anchor = when (base) {
                Any -> bounds.first
                is Single -> base.value
                is Range -> base.start
                is ListOf -> baseValues.first()
                is Step -> baseValues.first()
            }

            return baseValues.filter { (it - anchor) % step == 0 }.toSet()
        }
    }

    data class ListOf(val values: List<CronFieldSpec>) : CronFieldSpec {
        override fun resolve(bounds: IntRange): Set<Int> = values.flatMap { it.resolve(bounds) }.toSet()
    }

    companion object {
        fun parse(field: String): CronFieldSpec {
            val tokens = field.split(',').map { it.trim() }.filter { it.isNotBlank() }
            require(tokens.isNotEmpty()) { "Cron field cannot be empty" }

            val parsed = tokens.map { parseToken(it) }
            return if (parsed.size == 1) parsed.first() else ListOf(parsed)
        }

        private fun parseToken(token: String): CronFieldSpec {
            val stepParts = token.split('/')
            return when (stepParts.size) {
                1 -> parseBase(stepParts[0])
                2 -> {
                    val base = parseBase(stepParts[0])
                    val step = stepParts[1].toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid step value in token '$token'")
                    Step(base, step)
                }

                else -> throw IllegalArgumentException("Invalid token '$token'")
            }
        }

        private fun parseBase(base: String): CronFieldSpec {
            if (base == "*") return Any

            val rangeParts = base.split('-')
            return when (rangeParts.size) {
                1 -> {
                    val value = rangeParts[0].toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid numeric value '$base'")
                    Single(value)
                }

                2 -> {
                    val start = rangeParts[0].toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid range start in '$base'")
                    val end = rangeParts[1].toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid range end in '$base'")
                    Range(start, end)
                }

                else -> throw IllegalArgumentException("Invalid range '$base'")
            }
        }
    }
}
