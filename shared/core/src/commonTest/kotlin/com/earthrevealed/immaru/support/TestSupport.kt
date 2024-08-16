package com.earthrevealed.immaru.support

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


object FixedClock : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(0)
}