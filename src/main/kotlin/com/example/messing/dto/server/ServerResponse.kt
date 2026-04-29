package com.example.messing.dto.server

import com.example.messing.dto.user.UserResponse
import com.example.messing.entity.Server
import com.example.messing.entity.User
import java.time.Instant

// DTO gọn cho server list/overview ở FE sidebar.
data class ServerResponse(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val ownerId: String,
    val ownerUsername: String,
    val ownerDisplayName: String,
    val botUser: UserResponse?,
    val createdAt: Instant
) {
    companion object {
        fun from(server: Server, botUser: User? = null): ServerResponse {
            return ServerResponse(
                id = server.id!!,
                name = server.name,
                iconUrl = server.iconUrl,
                ownerId = server.owner?.id ?: "",
                ownerUsername = server.owner?.username ?: "",
                ownerDisplayName = server.owner?.displayName?.takeIf { it.isNotBlank() } ?: server.owner?.username ?: "",
                botUser = botUser?.let { UserResponse.from(it) },
                createdAt = server.createdAt
            )
        }
    }
}
