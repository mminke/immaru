package com.earthrevealed.immaru.support

import kotlin.time.Instant

fun Instant.truncateNanos() = Instant.fromEpochMilliseconds(this.toEpochMilliseconds())