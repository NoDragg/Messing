package com.example.messing.dto.message

import java.time.Instant

data class ChatMessageResponse(
    val id: String,
    val channelId: String,
    val content: String,
    val createdAt: Instant,
    val senderId: String,
    val senderUsername: String,
    val senderAvatarUrl: String?
)
