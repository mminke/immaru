package com.earthrevealed.immaru.support

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object FixedClock : Clock {
    val DEFAULT_INSTANT = Instant.parse("2000-01-01T01:00:00.000000000Z")

    private var _now: Instant = DEFAULT_INSTANT

    override fun now(): Instant {
        return _now
    }

    fun setClockTo(instant: Instant) {
        _now = instant
    }

    fun reset() {
        _now = DEFAULT_INSTANT
    }
}