package com.example.messing.dto.voice

data class VoiceParticipantStateDTO(
    val participantId: String,
    val userId: String,
    val channelId: String,
    val sessionId: String,
    val role: VoiceRole,
    val isMicEnabled: Boolean,
    val isMuted: Boolean,
    val isDeafened: Boolean,
    val isScreenSharing: Boolean,
    val screenShareTrackSid: String?,
    val screenShareSource: String?,
    val connectionState: VoiceConnectionState
)
