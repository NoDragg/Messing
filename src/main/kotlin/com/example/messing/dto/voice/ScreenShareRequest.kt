package com.example.messing.dto.voice

data class ScreenShareRequest(
    val channelId: String,
    val sessionId: String,
    val enabled: Boolean,
    val trackSid: String? = null,
    val source: String? = null
)
