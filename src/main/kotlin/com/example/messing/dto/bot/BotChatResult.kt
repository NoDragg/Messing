package com.example.messing.dto.bot

import com.example.messing.dto.message.ChatMessageResponse

data class BotChatResult(
    val userMessage: ChatMessageResponse,
    val botMessage: ChatMessageResponse
)