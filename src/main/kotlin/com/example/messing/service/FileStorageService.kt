package com.example.messing.service

import com.example.messing.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService(
    @Value("\${app.upload-dir:uploads}") uploadDir: String,
    @Value("\${app.public-base-url:http://localhost:8080}") private val publicBaseUrl: String
) {

    private val baseUploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    fun storeAvatar(file: MultipartFile, folder: String): String {
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
