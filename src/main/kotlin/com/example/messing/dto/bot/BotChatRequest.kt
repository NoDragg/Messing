package com.example.messing.dto.bot

data class BotChatRequest(
    val channelId: String,
    val question: String,
    val stream: Boolean = true
)