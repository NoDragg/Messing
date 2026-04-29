package com.example.messing.dto.user

import java.time.Instant

// Profile response phục vụ settings page và update UI.
data class UserProfileResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String?,
    val createdAt: Instant
)

data class UpdateProfileRequest(
    val displayName: String?,
    val bio: String?
)
