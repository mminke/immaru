package com.earthrevealed.immaru.files

import dev.zwander.kotlin.file.IPlatformFile
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.readAttributes

//actual val IPlatformFile.creationDate: Instant
//    get() {
//        val path = Paths.get(this.getAbsolutePath())
//        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
//        return attrs.creationTime().toInstant().toKotlinInstant()
//    }