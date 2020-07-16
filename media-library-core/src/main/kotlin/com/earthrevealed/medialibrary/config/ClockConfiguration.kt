package com.earthrevealed.medialibrary.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
private class ClockConfiguration {
    @Bean
    fun clock() = Clock.systemDefaultZone()
}