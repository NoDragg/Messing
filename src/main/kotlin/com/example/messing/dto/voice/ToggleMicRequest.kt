package com.example.messing.dto.voice

data class ToggleMicRequest(
    val channelId: String,
    val sessionId: String,
    val enabled: Boolean
)
