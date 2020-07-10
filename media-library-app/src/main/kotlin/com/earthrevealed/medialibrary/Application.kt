package com.earthrevealed.medialibrary

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableAutoConfiguration(exclude = [GsonAutoConfiguration::class])
class MediaLibraryApplication

fun main(args: Array<String>) {
    runApplication<MediaLibraryApplication>(*args)
}