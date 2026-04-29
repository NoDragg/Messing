package com.example.messing.dto.server

import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class UpdateServerBotRequest(
    @field:Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters")
    val displayName: String? = null,
    val avatar: MultipartFile? = null
)
