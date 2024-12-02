package com.earthrevealed.immaru.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
internal class ClockConfiguration {
    @Bean
    fun clock() = Clock.systemDefaultZone()
}