package com.example.messing.dto.bot

data class BotProfileDTO(
    val senderId: String = "bot:default",
    val senderUsername: String = "Messing Bot",
    val senderAvatarUrl: String? = null,
    val botModel: String = "Bot",
    val botProvider: String = "custom"
)