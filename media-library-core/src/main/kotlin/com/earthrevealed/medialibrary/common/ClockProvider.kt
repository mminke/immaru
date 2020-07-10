package com.earthrevealed.medialibrary.common

import org.springframework.stereotype.Service
import java.time.Clock

@Service
internal class ClockProvider(clock: Clock) {

    init {
        ClockProvider.clock = clock
    }

    companion object {
        lateinit var clock: Clock
    }
}