package com.earthrevealed.immaru.assets.plugins

import com.earthrevealed.immaru.assets.MediaType
import com.earthrevealed.immaru.assets.test.utils.useResourceAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.fail

class DetectMediaTypePluginTest {

    @Test
    fun `test processing of the byte flow produces the media type`() {
        val bytesFlow = flow {
            repeat(5) {
                delay(100)
                emit("Testje ${it + 1}".toByteArray())
            }
        }

        val detectMediaTypePlugin = DetectMediaTypePlugin()

        runBlocking {
            detectMediaTypePlugin.prepare()

            bytesFlow.collect { bytes ->
                detectMediaTypePlugin.processBytes(bytes)
            }

            detectMediaTypePlugin.finish()
        }

        assertEquals(MediaType.TEXT_PLAIN, detectMediaTypePlugin.result())
    }

    @Test
    fun `test exceptions are handled properly works correctly`() {
        val bytesFlow = flow {
            emit("Testje 1".toByteArray())
            emit("Testje 2".toByteArray())
            throw RuntimeException("Boom")
        }

        val detectMediaTypePlugin = DetectMediaTypePlugin()

        Assertions.assertThrows(RuntimeException::class.java) {
            runBlocking {
                detectMediaTypePlugin.prepare()

                bytesFlow.collect { bytes ->
                    detectMediaTypePlugin.processBytes(bytes)
                }

                fail("Exception should prevent reaching this code")
            }
        }

        runBlocking {
            detectMediaTypePlugin.finish()
        }
    }

    @Test
    fun `test reading a large jpg file`() {
        useResourceAsFlow("large.jpg") { jpgContent ->
            val detectMediaTypePlugin = DetectMediaTypePlugin()

            runBlocking {
                detectMediaTypePlugin.prepare()

                jpgContent.collect { bytes ->
                    detectMediaTypePlugin.processBytes(bytes)
                }

                detectMediaTypePlugin.finish()
            }

            assertEquals(MediaType.IMAGE_JPEG, detectMediaTypePlugin.result())
        }
    }

    @Test
    fun `test reading a large png file`() {
        useResourceAsFlow("large.png") { jpgContent ->
            val detectMediaTypePlugin = DetectMediaTypePlugin()

            runBlocking {
                detectMediaTypePlugin.prepare()

                jpgContent.collect { bytes ->
                    detectMediaTypePlugin.processBytes(bytes)
                }

                detectMediaTypePlugin.finish()
            }

            assertEquals(MediaType.IMAGE_PNG, detectMediaTypePlugin.result())
        }
    }
}
