package com.example.messing.dto.message

import com.example.messing.entity.MessageType
import java.time.Instant

// Response realtime cho chat history và websocket broadcast.
data class ChatMessageResponse(
    val id: String,
    val channelId: String,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
    val senderId: String,
    val senderUsername: String,
    val senderDisplayName: String?,
    val senderAvatarUrl: String?,
    val metadata: Map<String, Any?>? = null
)
