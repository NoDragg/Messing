package com.example.messing.dto.voice

import java.time.Instant

data class VoiceSessionDTO(
    val id: String,
    val channelId: String,
    val roomName: String,
    val status: VoiceSessionStatus,
    val createdAt: Instant
)

data class VoiceStateResponse(
    val session: VoiceSessionDTO?,
    val participants: List<VoiceParticipantStateDTO>,
    val speakerCount: Int,
    val listenerCount: Int,
    val activeSpeakerIds: List<String>
)
