package com.example.messing.service

import com.example.messing.dto.voice.VoiceSession
import com.example.messing.dto.voice.VoiceSessionStatus
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VoiceSessionService(
    private val store: VoiceStateStore
) {
    fun getOrCreateActiveSession(channelId: String): VoiceSession {
        return store.getOrCreateActiveSession(channelId)
    }

    fun markSessionEmpty(sessionId: String) {
        store.markSessionEmpty(sessionId)
    }

    fun endSession(channelId: String) {
        store.endSession(channelId)
    }

    fun getSessionByChannel(channelId: String): VoiceSession? {
        return store.getActiveSession(channelId)
    }
}
