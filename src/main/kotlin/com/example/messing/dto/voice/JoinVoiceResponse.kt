package com.example.messing.dto.voice

data class JoinVoiceResponse(
    val sessionId: String,
    val channelId: String,
    val roomName: String,
    val livekitUrl: String,
    val livekitToken: String,
    val listenOnly: Boolean,
    val participantState: VoiceParticipantStateDTO,
    val channelState: VoiceChannelStateDTO
)
