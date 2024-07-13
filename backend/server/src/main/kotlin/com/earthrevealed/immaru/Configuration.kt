package com.earthrevealed.immaru

import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.util.Properties

private val logger = KotlinLogging.logger { }

object Configuration {
    private val configuration = loadProperties("config/immaru.properties")

    object immaru {
        object server {
            val host = configuration.getProperty("immaru.server.host", "0.0.0.0")
            val port = configuration.getProperty("immaru.server.port", "8080").toInt()
        }

        object database {
            object r2dbc {
                val url = configuration.getProperty("immaru.database.r2dbc.url")
            }
        }
    }
}

fun loadProperties(filename: String): Properties = Properties().also {
    val file = File(filename)
    logger.info { "Loading properties from file: ${file.absoluteFile}" }
    it.load(
        FileInputStream(File(filename))
    )
    logger.debug { "Properties loaded:\n$it" }
}

class ConfigurationException(msg: String) : RuntimeException(msg)