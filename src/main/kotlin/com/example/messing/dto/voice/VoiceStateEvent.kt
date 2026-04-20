package com.example.messing.dto.voice

import java.time.Instant

enum class VoiceEventType {
    JOIN_REQUESTED, JOIN_ACCEPTED, JOIN_DENIED, LEFT,
    MIC_ENABLED, MIC_DISABLED, SESSION_CREATED, SESSION_ENDED,
    RECONNECTED, DISCONNECTED, STATE_BROADCASTED
}

data class VoiceStateEvent(
    val id: String,
    val sessionId: String? = null,
    val channelId: String? = null,
    val userId: String? = null,
    val eventType: VoiceEventType,
    val payload: String? = null,
    val createdAt: Instant = Instant.now()
)
