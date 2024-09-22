package com.earthrevealed.immaru.common

import kotlinx.datetime.Clock

object ClockProvider {
    var clock: Clock = Clock.System
}