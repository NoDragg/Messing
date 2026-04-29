package com.example.messing.dto.auth

// Payload chuẩn hóa để FE chỉ cần 1 response cho cả register/login.
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val displayName: String?,
    val email: String
)
