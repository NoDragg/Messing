package com.example.messing.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Username or email is required")
    val identifier: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)
