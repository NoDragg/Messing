package com.example.messing.dto.message

import com.example.messing.entity.MessageType
import java.time.Instant

data class ChatMessageResponse(
    val id: String,
    val channelId: String,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
    val senderId: String,
    val senderUsername: String,
    val senderAvatarUrl: String?
)
