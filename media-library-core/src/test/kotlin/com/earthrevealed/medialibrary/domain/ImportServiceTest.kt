package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import com.earthrevealed.medialibrary.persistence.AssetRepository
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
internal class ImportServiceTest {
    @TempDir lateinit var temporaryFolder: Path

    @Mock lateinit var assetRepositoryMock: AssetRepository

    @BeforeEach
    fun initializeClock() {
        ClockProvider(Clock.systemDefaultZone())
    }

    @Test
    fun testImport() {
        val importer = ImportService(temporaryFolder.toString(), assetRepositoryMock)

        importer.importFrom(Path.of("./src/test/resources/images"))

        assertThat {
            Files.walk(temporaryFolder)
                    .filter{ Files.isRegularFile(it)}
                    .collect(Collectors.toList())
                    .size `should be equal to` 3
        }

        verify(assetRepositoryMock, times(3)).save(any())
    }
}

fun assertThat(action: () -> Unit) {
    action()
}