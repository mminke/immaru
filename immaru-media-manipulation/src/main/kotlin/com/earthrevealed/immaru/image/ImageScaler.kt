package com.earthrevealed.immaru.image

import org.imgscalr.Scalr
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

const val THUMBNAIL_SIZE = 300

fun scaleImage(imageInputStream: InputStream): ByteArrayOutputStream {
    ImageIO.setUseCache(false)
    val srcImage = ImageIO.read(imageInputStream)
    val scaledImage = Scalr.resize(srcImage, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, THUMBNAIL_SIZE)

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(scaledImage, "png", outputStream)
    outputStream.flush()
    return outputStream
}

fun convertToPng(inputStream: InputStream, outputStream: OutputStream) {
    ImageIO.setUseCache(false)
    val srcImage = ImageIO.read(inputStream)
    ImageIO.write(srcImage, "png", outputStream)
    outputStream.flush()
}

