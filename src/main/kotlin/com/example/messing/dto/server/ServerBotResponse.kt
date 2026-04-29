package com.example.messing.dto.server

data class ServerBotResponse(
    val serverId: String,
    val botUserId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isVirtual: Boolean
)
