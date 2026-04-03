package com.example.messing.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email hoặc login name là bắt buộc")
    val identifier: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)
