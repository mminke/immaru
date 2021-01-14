package com.earthrevealed.immaru

import com.earthrevealed.immaru.domain.collection
import com.earthrevealed.immaru.persistence.exposed.AssetTable
import org.amshove.kluent.`should be equal to`
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

@SpringBootTest
class CollectionServiceIT {
    @Autowired lateinit var collectionService: CollectionService
    @Value("\${immaru.library.path}") lateinit var libraryPathValue: String

    private val importFromLocation = Path.of("./src/test/resources/images")

    @BeforeEach
    fun clearData() {
        transaction {
            AssetTable.deleteAll()
        }
        FileSystemUtils.deleteRecursively(Path.of(libraryPathValue))
    }

    @Test
    fun testImport() {
        val collection = collection { }.also { collectionService.save(it) }
        collectionService.importFrom(importFromLocation) into collection

        assertThat {
            Files.walk(Path.of(libraryPathValue))
                    .filter{ Files.isRegularFile(it)}
                    .collect(Collectors.toList())
                    .size `should be equal to` 3
        }
    }
}