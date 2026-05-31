package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.AssetProcessingPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.time.Instant

class DetectOriginalCreatedAtPlugin : AssetProcessingPlugin {
    private val pipedOutputStream = PipedOutputStream()
    private val pipedInputStream = PipedInputStream(pipedOutputStream)
    private var originalCreatedAt: Instant? = null
    private lateinit var job: Thread

    override suspend fun prepare() {
        job = thread {
            val metadata = Metadata()

            try {
                AutoDetectParser().parse(pipedInputStream, DefaultHandler(), metadata, ParseContext())
            } catch (_: Throwable) {
                // Best effort: metadata extraction should never fail uploads.
            } finally {
                pipedInputStream.close()
            }

            originalCreatedAt = detectOriginalCreatedAt(metadata)
        }
    }

    override suspend fun processBytes(bytes: ByteArray) {
        try {
            if (originalCreatedAt == null) {
                withContext(Dispatchers.IO) {
                    pipedOutputStream.write(bytes)
                }
            }
        } catch (_: IOException) {
            // Ignore IOExceptions (Pipe closed exception is expected)
        }
    }

    override suspend fun finish() {
        withContext(Dispatchers.IO) {
            pipedOutputStream.close()
            job.join(1000)
        }
    }

    fun result(): Instant? {
        return originalCreatedAt
    }
}

internal fun detectOriginalCreatedAt(metadata: Metadata): Instant? {
    val candidates = buildList {
        addAll(metadata.getValues(TikaCoreProperties.CREATED).toList())
        addAll(metadata.getValues("Date/Time Original").toList())
        addAll(metadata.getValues("Creation-Date").toList())
        addAll(metadata.getValues("dcterms:created").toList())
        addAll(metadata.getValues("date").toList())
    }

    return candidates.firstNotNullOfOrNull(::parseMetadataInstant)
}

internal fun parseMetadataInstant(value: String): Instant? {
    val normalized = value.trim()
    if (normalized.isEmpty()) {
        return null
    }

    return runCatching { Instant.parse(normalized) }.getOrNull()
        ?: runCatching { Instant.parse(OffsetDateTime.parse(normalized).toInstant().toString()) }.getOrNull()
        ?: runCatching { Instant.parse(ZonedDateTime.parse(normalized).toInstant().toString()) }.getOrNull()
        ?: exifDateFormats.firstNotNullOfOrNull { dateFormat ->
            runCatching {
                Instant.parse(
                    LocalDateTime.parse(normalized, dateFormat)
                        .toInstant(ZoneOffset.UTC)
                        .toString()
                )
            }.getOrNull()
        }
}

private val exifDateFormats = listOf(
    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
)
