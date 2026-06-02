package com.earthrevealed.immaru.assets.library

import kotlinx.coroutines.runBlocking
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertNull

class DetectOriginalCreatedAtPluginTest {
	private val expectedCreatedAt = kotlin.time.Instant.parse("2009-08-17T05:51:04Z")

	@Test
	fun `test extracting original creation date from exif metadata`() {
		val metadata = Metadata().apply {
			set("Date/Time Original", "2024:01:02 03:04:05")
		}

		val result = detectOriginalCreatedAt(metadata)

		assertEquals(kotlin.time.Instant.parse("2024-01-02T03:04:05Z"), result)
	}

	@Test
	fun `test extracting original creation date from tika metadata`() {
		val metadata = Metadata().apply {
			set(TikaCoreProperties.CREATED, "2024-01-02T03:04:05+02:00")
		}

		val result = detectOriginalCreatedAt(metadata)

		assertEquals(kotlin.time.Instant.parse("2024-01-02T01:04:05Z"), result)
	}

	@Test
	fun `test extracting original creation date returns null when missing`() {
		val metadata = Metadata().apply {
			set("resourceName", "test.jpg")
		}

		val result = detectOriginalCreatedAt(metadata)

		assertNull(result)
	}

	@Test
	fun `test reading a jpg file for original creation date`() {
		useResourceAsFlow("1px.jpg") { jpgContent ->
			val detectOriginalCreatedAtPlugin = DetectOriginalCreatedAtPlugin()

			runBlocking {
				detectOriginalCreatedAtPlugin.prepare()

				jpgContent.collect { bytes ->
					detectOriginalCreatedAtPlugin.processBytes(bytes)
				}

				detectOriginalCreatedAtPlugin.finish()
			}

			assertEquals(expectedCreatedAt, detectOriginalCreatedAtPlugin.result())
		}
	}

	@Test
	fun `test reading a png file for original creation date`() {
		useResourceAsFlow("1px.png") { pngContent ->
			val detectOriginalCreatedAtPlugin = DetectOriginalCreatedAtPlugin()

			runBlocking {
				detectOriginalCreatedAtPlugin.prepare()

				pngContent.collect { bytes ->
					detectOriginalCreatedAtPlugin.processBytes(bytes)
				}

				detectOriginalCreatedAtPlugin.finish()
			}

			assertEquals(expectedCreatedAt, detectOriginalCreatedAtPlugin.result())
		}
	}

	@Test
	fun `test reading a tiff file for original creation date`() {
		useResourceAsFlow("1px.tif") { tifContent ->
			val detectOriginalCreatedAtPlugin = DetectOriginalCreatedAtPlugin()

			runBlocking {
				detectOriginalCreatedAtPlugin.prepare()

				tifContent.collect { bytes ->
					detectOriginalCreatedAtPlugin.processBytes(bytes)
				}

				detectOriginalCreatedAtPlugin.finish()
			}

			assertEquals(expectedCreatedAt, detectOriginalCreatedAtPlugin.result())
		}
	}
}
