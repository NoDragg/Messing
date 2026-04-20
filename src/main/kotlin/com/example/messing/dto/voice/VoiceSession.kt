package com.example.messing.dto.voice

import java.time.Instant

enum class VoiceSessionStatus {
    ACTIVE, EMPTY, ENDED
}

data class VoiceSession(
    val id: String,
    val channelId: String,
    val roomName: String,
    var status: VoiceSessionStatus = VoiceSessionStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    var endedAt: Instant? = null
)
