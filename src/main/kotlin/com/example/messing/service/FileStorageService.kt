package com.example.messing.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.messing.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import javax.imageio.ImageIO

@Service
class FileStorageService(
    @Value("\${app.upload-dir:}") uploadDir: String,
    @Value("\${app.public-base-url:}") private val publicBaseUrl: String,
    @Value("\${app.avatar-size:}") private val avatarSizePx: Int,
    @Value("\${app.storage.provider:}") private val storageProvider: String,
    @Value("\${app.cloudinary.cloud-name:}") private val cloudName: String,
    @Value("\${app.cloudinary.api-key:}") private val apiKey: String,
    @Value("\${app.cloudinary.api-secret:}") private val apiSecret: String
) {

    private val baseUploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    private val cloudinary: Cloudinary? by lazy {
        if (storageProvider.equals("cloudinary", ignoreCase = true)) {
            require(cloudName.isNotBlank()) { "Missing app.cloudinary.cloud-name" }
            require(apiKey.isNotBlank()) { "Missing app.cloudinary.api-key" }
            require(apiSecret.isNotBlank()) { "Missing app.cloudinary.api-secret" }

            Cloudinary(
                ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
                )
            )
        } else {
            null
        }
    }

    fun storeCircularAvatar(file: MultipartFile, folder: String): String {
        validateImage(file)

        val sourceImage = file.inputStream.use { input ->
            ImageIO.read(input)
        } ?: throw BadRequestException("Unable to read image data")

        val squareImage = cropToSquare(sourceImage)
        val resizedSquareImage = resizeImage(squareImage, avatarSizePx, avatarSizePx)

        return if (storageProvider.equals("cloudinary", ignoreCase = true)) {
            uploadAvatarToCloudinary(resizedSquareImage, folder)
        } else {
            storeAvatarLocally(resizedSquareImage, folder)
        }
    }

    fun storeChatImage(file: MultipartFile, folder: String): String {
        validateImage(file)

        return if (storageProvider.equals("cloudinary", ignoreCase = true)) {
            uploadChatImageToCloudinary(file, folder)
        } else {
            storeChatImageLocally(file, folder)
        }
    }

    private fun uploadAvatarToCloudinary(image: BufferedImage, folder: String): String {
        val cloudinaryClient = cloudinary ?: throw IllegalStateException("Cloudinary is not initialized")

        val tempFile = Files.createTempFile("avatar-", ".png").toFile()
        try {
            ImageIO.write(image, "png", tempFile)

            val result = cloudinaryClient.uploader().upload(
                tempFile,
                ObjectUtils.asMap(
                    "folder", "messing/$folder",
                    "public_id", UUID.randomUUID().toString(),
                    "overwrite", true,
                    "resource_type", "image"
                )
            )

            return (result["secure_url"] as? String)
                ?: throw IllegalStateException("Cloudinary did not return secure_url")
        } finally {
            tempFile.delete()
        }
    }

    private fun uploadChatImageToCloudinary(file: MultipartFile, folder: String): String {
        val cloudinaryClient = cloudinary ?: throw IllegalStateException("Cloudinary is not initialized")

        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"

        val result = cloudinaryClient.uploader().upload(
            file.bytes,
            ObjectUtils.asMap(
                "folder", "messing/$folder",
                "public_id", UUID.randomUUID().toString(),
                "format", extension,
                "overwrite", true,
                "resource_type", "image"
            )
        )

        return (result["secure_url"] as? String)
            ?: throw IllegalStateException("Cloudinary did not return secure_url")
    }

    private fun storeAvatarLocally(image: BufferedImage, folder: String): String {
        val targetDir = baseUploadPath.resolve(folder).normalize()
        Files.createDirectories(targetDir)

        val fileName = "${UUID.randomUUID()}.png"
        val targetPath = targetDir.resolve(fileName)

        ImageIO.write(image, "png", targetPath.toFile())

        return "${publicBaseUrl.trimEnd('/')}/media/$folder/$fileName"
    }

    private fun storeChatImageLocally(file: MultipartFile, folder: String): String {
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

    private fun cropToSquare(image: BufferedImage): BufferedImage {
        val size = minOf(image.width, image.height)
        val x = (image.width - size) / 2
        val y = (image.height - size) / 2

        return image.getSubimage(x, y, size, size)
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
            throw BadRequestException("Image file must not be empty")
        }

        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw BadRequestException("Only image files are allowed")
        }
    }
}
