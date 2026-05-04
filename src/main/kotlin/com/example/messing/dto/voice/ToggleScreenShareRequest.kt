package com.example.messing.dto.voice

data class ToggleScreenShareRequest(
    val channelId: String,
    val sessionId: String,
    val enabled: Boolean,
    val trackSid: String? = null,
    val source: String? = null
)
