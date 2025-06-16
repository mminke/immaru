package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.AssetProcessingPlugin
import com.earthrevealed.immaru.assets.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread

class DetectMediaTypePlugin : AssetProcessingPlugin {
    private val pipedOutputStream = PipedOutputStream()
    private val pipedInputStream = PipedInputStream(pipedOutputStream)
    private var mediaType: MediaType? = null
    private lateinit var job: Thread

    override suspend fun prepare() {
        job = thread {
            val detectedMimeType = Tika().detect(pipedInputStream, Metadata())
            mediaType = MediaType.parse(detectedMimeType.toString())
            pipedInputStream.close()
        }
    }

    override suspend fun processBytes(bytes: ByteArray) {
        try {
            if(mediaType == null) {
                withContext(Dispatchers.IO) {
                    pipedOutputStream.write(bytes)
                }
            }
        } catch (exception: IOException) {
            // Ignore IOExceptions (Pipe closed exception is expected)
        }
    }

    override suspend fun finish() {
        withContext(Dispatchers.IO) {
            pipedOutputStream.close()
            job.join(1000)
        }
    }

    fun result(): MediaType {
        return mediaType!!
    }
}
