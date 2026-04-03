package com.example.messing.dto.auth

data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val loginName: String,
    val email: String
)
