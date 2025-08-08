package com.earthrevealed.immaru.common

import kotlin.time.Clock

object ClockProvider {
    var clock: Clock = Clock.System
}