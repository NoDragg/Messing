package com.example.messing.mapper

import com.example.messing.dto.auth.AuthResponse
import com.example.messing.dto.server.ServerBotResponse
import com.example.messing.dto.user.UserProfileResponse
import com.example.messing.entity.Server
import com.example.messing.entity.User

object UserMapper {

    fun toAuthResponse(user: User, token: String): AuthResponse {
        return AuthResponse(
            token = token,
            userId = user.id!!,
            username = user.username,
            displayName = displayNameOrUsername(user),
            email = user.email
        )
    }

    fun toProfileResponse(user: User): UserProfileResponse {
        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            displayName = displayNameOrUsername(user),
            email = user.email,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            createdAt = user.createdAt
        )
    }

    fun toServerBotResponse(serverId: String, user: User): ServerBotResponse {
        return ServerBotResponse(
            serverId = serverId,
            botUserId = user.id!!,
            username = user.username,
            displayName = displayNameOrUsername(user),
            avatarUrl = user.avatarUrl,
            isVirtual = user.isVirtual
        )
    }

    fun displayNameOrUsername(user: User): String {
        return user.displayName?.takeIf { it.isNotBlank() } ?: user.username
    }

    fun ensureBotIdentity(server: Server, botUser: User) {
        botUser.username = "bot:server:${server.id}"
        botUser.displayName = botUser.displayName?.takeIf { it.isNotBlank() } ?: "Messing Bot"
        botUser.email = botUser.username
        botUser.isVirtual = true
    }
}
