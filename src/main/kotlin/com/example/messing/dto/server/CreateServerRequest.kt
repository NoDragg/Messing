package com.example.messing.dto.server

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateServerRequest(
    @field:NotBlank(message = "Server name is required")
    @field:Size(min = 1, max = 100, message = "Server name must be between 1 and 100 characters")
    val name: String,

    val iconUrl: String? = null
)
