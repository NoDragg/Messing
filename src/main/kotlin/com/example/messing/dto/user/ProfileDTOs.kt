package com.example.messing.dto.user

import java.time.Instant

data class UserProfileResponse(
    val id: String,
    val username: String,
    val loginName: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String?,
    val createdAt: Instant
)

data class UpdateProfileRequest(
    val username: String?,
    val bio: String?
)
