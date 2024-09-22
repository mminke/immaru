package com.earthrevealed.immaru

import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
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
            object flyway {
                object jdbc {
                    val url = configuration.getProperty("immaru.database.flyway.jdbc.url")
                    val username = configuration.getProperty("immaru.database.flyway.jdbc.username")
                    val password = configuration.getProperty("immaru.database.flyway.jdbc.password")
                }
            }
        }

        object library {
            val path = Path.of(configuration.getProperty("immaru.library.root.path"))
        }
    }
}

private fun loadProperties(filename: String): Properties = Properties().also {
    val file = File(filename)
    logger.info { "Loading properties from file: ${file.absoluteFile}" }
    it.load(
        FileInputStream(File(filename))
    )
    logger.debug { "Properties loaded:\n$it" }
}
