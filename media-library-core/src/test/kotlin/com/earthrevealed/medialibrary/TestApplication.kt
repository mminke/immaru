package com.earthrevealed.medialibrary

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableAutoConfiguration(exclude = [GsonAutoConfiguration::class])
class TestApplication {

    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway: Flyway ->
            flyway.migrate()
        }
    }
}