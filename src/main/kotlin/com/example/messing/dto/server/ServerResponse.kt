package com.example.messing.dto.server

import com.example.messing.entity.Server
import java.time.Instant

data class ServerResponse(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val ownerId: String,
    val ownerUsername: String,
    val createdAt: Instant
) {
    companion object {
        fun from(server: Server): ServerResponse {
            return ServerResponse(
                id = server.id!!,
                name = server.name,
                iconUrl = server.iconUrl,
                ownerId = server.owner?.id ?: "",
                ownerUsername = server.owner?.username ?: "",
                createdAt = server.createdAt
            )
        }
    }
}
