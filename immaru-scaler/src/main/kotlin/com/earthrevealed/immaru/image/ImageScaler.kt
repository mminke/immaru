package com.earthrevealed.immaru.image

import org.imgscalr.Scalr
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

fun scaleImage(imageInputStream: InputStream): ByteArrayOutputStream {
    ImageIO.setUseCache(false)
    val srcImage = ImageIO.read(imageInputStream)
    val scaledImage = Scalr.resize(srcImage, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC,300)

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(scaledImage, "png", outputStream)
    outputStream.flush()
    return outputStream
}