package com.earthrevealed.immaru.video

import com.earthrevealed.immaru.image.THUMBNAIL_SIZE
import org.imgscalr.Scalr
import org.jcodec.api.FrameGrab
import org.jcodec.scale.AWTUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO

fun extractThumbnail(path: Path): ByteArrayOutputStream {
    val frameNumber = Int.MAX_VALUE

    val picture = FrameGrab.getFrameFromFile(path.toFile(), frameNumber)
    val bufferedImage: BufferedImage = AWTUtil.toBufferedImage(picture)
    val scaledImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, THUMBNAIL_SIZE)

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(scaledImage, "png", outputStream)
    outputStream.flush()
    return outputStream
}