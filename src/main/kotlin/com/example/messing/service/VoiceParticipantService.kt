package com.example.messing.service

import com.example.messing.dto.voice.VoiceParticipant
import com.example.messing.dto.voice.VoiceRole
import com.example.messing.dto.voice.VoiceConnectionState
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class VoiceParticipantService(
    private val store: VoiceStateStore
) {
    fun upsertJoinState(userId: String, channelId: String, sessionId: String, listenOnly: Boolean): VoiceParticipant {
        val existing = store.getParticipant(sessionId, userId)
        val participant = existing?.copy(
            connectionState = VoiceConnectionState.CONNECTED,
            lastSeenAt = Instant.now(),
            role = if (listenOnly) VoiceRole.LISTENER else VoiceRole.SPEAKER,
            isMicEnabled = !listenOnly
        ) ?: VoiceParticipant(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            channelId = channelId,
            userId = userId,
            role = if (listenOnly) VoiceRole.LISTENER else VoiceRole.SPEAKER,
            isMicEnabled = !listenOnly,
            connectionState = VoiceConnectionState.CONNECTED
        )
        store.upsertParticipant(participant)
        return participant
    }

    fun markMicEnabled(userId: String, sessionId: String, enabled: Boolean): VoiceParticipant? {
        val p = store.getParticipant(sessionId, userId) ?: return null
        val updated = p.copy(
            isMicEnabled = enabled,
            role = if (enabled) VoiceRole.SPEAKER else VoiceRole.LISTENER
        )
        store.upsertParticipant(updated)
        return updated
    }

    fun markParticipantLeft(userId: String, sessionId: String) {
        val p = store.getParticipant(sessionId, userId) ?: return
        val updated = p.copy(
            connectionState = VoiceConnectionState.DISCONNECTED,
            leftAt = Instant.now()
        )
        store.upsertParticipant(updated)
    }

    fun markDisconnected(userId: String, sessionId: String) {
        val p = store.getParticipant(sessionId, userId) ?: return
        store.upsertParticipant(p.copy(connectionState = VoiceConnectionState.DISCONNECTED))
    }

    fun markReconnected(userId: String, sessionId: String) {
        val p = store.getParticipant(sessionId, userId) ?: return
        store.upsertParticipant(p.copy(
            connectionState = VoiceConnectionState.CONNECTED,
            lastSeenAt = Instant.now()
        ))
    }

    fun getParticipantState(userId: String, sessionId: String): VoiceParticipant? {
        return store.getParticipant(sessionId, userId)
    }

    fun getActiveParticipants(sessionId: String): List<VoiceParticipant> {
        return store.getActiveParticipants(sessionId)
    }
}
