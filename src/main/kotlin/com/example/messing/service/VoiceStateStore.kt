package com.example.messing.service

import com.example.messing.dto.voice.VoiceParticipant
import com.example.messing.dto.voice.VoiceSession
import com.example.messing.dto.voice.VoiceSessionStatus
import com.example.messing.dto.voice.VoiceConnectionState
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory store cho toàn bộ voice state.
 * Dữ liệu chỉ tồn tại trên RAM — đủ cho FE đọc realtime state,
 * không cần persistence vì voice state sẽ reset khi server restart.
 *
 * Structure:
 *   sessions:     channelId -> VoiceSession
 *   participants: sessionId -> (userId -> VoiceParticipant)
 */
@Component
class VoiceStateStore {

    // Mỗi channel tối đa 1 session đang ACTIVE tại một thời điểm
    private val sessions = ConcurrentHashMap<String, VoiceSession>()

    // Nested map: sessionId -> userId -> participant
    private val participants = ConcurrentHashMap<String, ConcurrentHashMap<String, VoiceParticipant>>()

    // ────────────── Session ──────────────

    fun getActiveSession(channelId: String): VoiceSession? {
        return sessions[channelId]?.takeIf { it.status == VoiceSessionStatus.ACTIVE }
    }

    fun getOrCreateActiveSession(channelId: String): VoiceSession {
        return sessions.compute(channelId) { _, existing ->
            if (existing != null && existing.status == VoiceSessionStatus.ACTIVE) existing
            else VoiceSession(
                id = UUID.randomUUID().toString(),
                channelId = channelId,
                roomName = "voice-$channelId",
                status = VoiceSessionStatus.ACTIVE
            )
        }!!
    }

    fun markSessionEmpty(sessionId: String) {
        sessions.values.find { it.id == sessionId }?.let { it.status = VoiceSessionStatus.EMPTY }
    }

    fun endSession(channelId: String) {
        sessions.remove(channelId)
        // Xóa tất cả participants của session đó
        val sessionId = sessions[channelId]?.id ?: return
        participants.remove(sessionId)
    }

    // ────────────── Participant ──────────────

    fun getParticipant(sessionId: String, userId: String): VoiceParticipant? {
        return participants[sessionId]?.get(userId)
    }

    fun upsertParticipant(participant: VoiceParticipant) {
        participants
            .getOrPut(participant.sessionId) { ConcurrentHashMap() }[participant.userId] = participant
    }

    fun removeParticipant(sessionId: String, userId: String) {
        participants[sessionId]?.remove(userId)
    }

    fun getActiveParticipants(sessionId: String): List<VoiceParticipant> {
        return participants[sessionId]?.values
            ?.filter { it.connectionState == VoiceConnectionState.CONNECTED }
            ?: emptyList()
    }

    fun getAllParticipants(sessionId: String): List<VoiceParticipant> {
        return participants[sessionId]?.values?.toList() ?: emptyList()
    }

    fun clearSession(sessionId: String) {
        participants.remove(sessionId)
    }
}
