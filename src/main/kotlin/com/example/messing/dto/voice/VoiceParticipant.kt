package com.example.messing.dto.voice

import java.time.Instant

enum class VoiceRole {
    SPEAKER, LISTENER
}

enum class VoiceConnectionState {
    CONNECTED, DISCONNECTED, RECONNECTING
}

data class VoiceParticipant(
    val id: String,
    val sessionId: String,
    val channelId: String,
    val userId: String,
    var role: VoiceRole = VoiceRole.LISTENER,
    var isMicEnabled: Boolean = false,
    var isMuted: Boolean = false,
    var isDeafened: Boolean = false,
    var isScreenSharing: Boolean = false,
    var screenShareTrackSid: String? = null,
    var screenShareSource: String? = null,
    var connectionState: VoiceConnectionState = VoiceConnectionState.CONNECTED,
    val joinedAt: Instant = Instant.now(),
    var leftAt: Instant? = null,
    var lastSeenAt: Instant = Instant.now(),
    var livekitIdentity: String? = null
)
