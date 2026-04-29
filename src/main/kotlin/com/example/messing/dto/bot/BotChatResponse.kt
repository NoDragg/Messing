package com.example.messing.dto.bot

import com.example.messing.entity.MessageType
import java.time.Instant

data class BotChatResponse(
    val id: String,
    val channelId: String,
    val content: String,
    val type: MessageType = MessageType.BOT,
    val createdAt: Instant,
    val senderId: String? = null,
    val senderUsername: String = "Bot",
    val senderAvatarUrl: String? = null
)