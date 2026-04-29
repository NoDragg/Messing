package com.example.messing.dto.user

import com.example.messing.entity.User

data class UserResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String?,
    val isVirtual: Boolean
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id ?: "",
                username = user.username,
                displayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.username,
                email = user.email,
                avatarUrl = user.avatarUrl,
                bio = user.bio,
                isVirtual = user.isVirtual
            )
        }
    }
}
