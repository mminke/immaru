package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.util.stream.Collectors

internal class ImporterTest {
    @TempDir
    lateinit var temporaryFolder: Path

    @BeforeEach
    fun initializeClock() {
        ClockProvider(Clock.systemDefaultZone())
    }

    @Test
    fun testImport() {
        val importer = Importer(temporaryFolder.toString())

        importer.importFrom(Path.of("./src/test/resources/images"))

        assertThat {
            Files.walk(temporaryFolder)
                    .filter{ Files.isRegularFile(it)}
                    .collect(Collectors.toList())
                    .size `should be equal to` 3
        }
    }
}

fun assertThat(action: () -> Unit) {
    action()
}