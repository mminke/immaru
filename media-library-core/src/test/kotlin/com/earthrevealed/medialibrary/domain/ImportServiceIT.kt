package com.earthrevealed.medialibrary.domain

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

@SpringBootTest
class ImportServiceIT {
    @Autowired lateinit var importService: ImportService
    @Value("\${media-library.library.path}") lateinit var libraryPathValue: String


    @Test
    fun testImport() {
        importService.importFrom(Path.of("./src/test/resources/images"))

        assertThat {
            Files.walk(Path.of(libraryPathValue))
                    .filter{ Files.isRegularFile(it)}
                    .collect(Collectors.toList())
                    .size `should be equal to` 3
        }
    }
}