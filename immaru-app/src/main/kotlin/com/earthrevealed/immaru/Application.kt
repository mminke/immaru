package com.earthrevealed.immaru

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableAutoConfiguration(exclude = [GsonAutoConfiguration::class])
class ImmaruApplication

fun main(args: Array<String>) {
    runApplication<ImmaruApplication>(*args)
}