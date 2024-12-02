package com.earthrevealed.immaru.common

import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import java.nio.file.Path

object tika : Tika()

fun Path.mediaType() = this.toFile().readBytes().mediaType()

fun MediaType.isSupported() = this.type == "image" || this.type == "video"

fun ByteArray.mediaType() = MediaType.parse(tika.detect(this))
