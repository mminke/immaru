package com.earthrevealed.immaru.assets.plugins

import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.Tag
import com.earthrevealed.immaru.assets.AssetProcessingPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread

class JpegParseMetadataPlugin : AssetProcessingPlugin {
    private val pipedOutputStream = PipedOutputStream()
    private val pipedInputStream = PipedInputStream(pipedOutputStream)
    private var metadata: List<String>? = null
    private lateinit var job: Thread

    override suspend fun prepare() {
        job = thread {
            metadata = JpegMetadataReader.readMetadata(pipedInputStream).directories
                .flatMap { directory: Directory ->
                    directory.tags.map { tag: Tag -> "${directory.name} -> ${tag.tagName}" }
                }

            pipedInputStream.close()
        }
    }

    override suspend fun processBytes(bytes: ByteArray) {
        try {
            if (metadata == null) {
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

    fun result(): List<String> {
        return metadata!!
    }
}