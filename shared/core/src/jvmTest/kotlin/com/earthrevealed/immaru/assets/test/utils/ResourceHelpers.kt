package com.earthrevealed.immaru.assets.test.utils

import com.earthrevealed.immaru.common.io.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.junit.Test
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

fun useResourceAsFlow(name: String, useFlowBlock: (flow: Flow<ByteArray>) -> Unit) {
    Test::class.java.classLoader?.getResourceAsStream(name)!!.use { inputStream ->
        val inputFlow = inputStream.asSource().buffered().toFlow()

        useFlowBlock(inputFlow)
    }
}

fun resourceAsPath(name: String): Path = Test::class.java.classLoader!!.getResource(name).toPath()

private fun URL.toPath() = Paths.get(toURI())