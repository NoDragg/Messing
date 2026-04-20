package com.example.messing.dto.voice

data class VoiceChannelStateDTO(
    val channelId: String,
    val sessionId: String?,
    val participantCount: Int,
    val speakerCount: Int,
    val listenerCount: Int,
    val activeSpeakerIds: List<String>,
    val roomName: String?
)
