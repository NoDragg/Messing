package com.example.messing.dto.voice

data class JoinVoiceRequest(
    val channelId: String,
    val wantMic: Boolean
)
