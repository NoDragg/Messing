package com.example.messing.service

import com.example.messing.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.AlphaComposite
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import javax.imageio.ImageIO

@Service
class FileStorageService(
    @Value("\${app.upload-dir:uploads}") uploadDir: String,
    @Value("\${app.public-base-url:http://localhost:8080}") private val publicBaseUrl: String,
    @Value("\${app.avatar-size:256}") private val avatarSizePx: Int
) {

    private val baseUploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    fun storeCircularAvatar(file: MultipartFile, folder: String): String {
        validateImage(file)

        val sourceImage = file.inputStream.use { input ->
            ImageIO.read(input)
        } ?: throw BadRequestException("Không đọc được dữ liệu ảnh")

        val circularImage = cropToCircle(sourceImage)
        val resizedCircularImage = resizeImage(circularImage, avatarSizePx, avatarSizePx)

        val targetDir = baseUploadPath.resolve(folder).normalize()
        Files.createDirectories(targetDir)

        val fileName = "${UUID.randomUUID()}.png"
        val targetPath = targetDir.resolve(fileName)

        ImageIO.write(resizedCircularImage, "png", targetPath.toFile())

        return "${publicBaseUrl.trimEnd('/')}/media/$folder/$fileName"
    }

    fun storeChatImage(file: MultipartFile, folder: String): String {
        validateImage(file)

        val targetDir = baseUploadPath.resolve(folder).normalize()
        Files.createDirectories(targetDir)

        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"

        val fileName = "${UUID.randomUUID()}.$extension"
        val targetPath = targetDir.resolve(fileName)

        file.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        return "${publicBaseUrl.trimEnd('/')}/media/$folder/$fileName"
    }

    private fun cropToCircle(image: BufferedImage): BufferedImage {
        val size = minOf(image.width, image.height)
        val x = (image.width - size) / 2
        val y = (image.height - size) / 2

        val square = image.getSubimage(x, y, size, size)
        val output = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)

        val g2 = output.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.clip = Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
        g2.composite = AlphaComposite.SrcOver
        g2.drawImage(square, 0, 0, null)
        g2.dispose()

        return output
    }

    private fun resizeImage(source: BufferedImage, width: Int, height: Int): BufferedImage {
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = resized.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(source, 0, 0, width, height, null)
        g2.dispose()
        return resized
    }

    private fun validateImage(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("File ảnh không được để trống")
        }

        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw BadRequestException("Chỉ chấp nhận file ảnh")
        }
    }
}
