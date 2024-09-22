package com.earthrevealed.immaru.support

import kotlinx.datetime.Instant

fun Instant.truncateNanos() = Instant.fromEpochMilliseconds(this.toEpochMilliseconds())