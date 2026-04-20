package com.example.messing.service

import com.example.messing.dto.voice.*
import com.example.messing.dto.voice.VoiceRole
import com.example.messing.dto.voice.VoiceParticipant
import com.example.messing.dto.voice.VoiceSessionStatus
import org.springframework.stereotype.Service

@Service
class VoiceChannelStateService(
    private val store: VoiceStateStore
) {
    fun buildChannelState(channelId: String): VoiceChannelStateDTO {
        val session = store.getActiveSession(channelId)
            ?: return VoiceChannelStateDTO(channelId, null, 0, 0, 0, emptyList(), null)

        val participants = store.getActiveParticipants(session.id)
        val speakers = participants.filter { it.role == VoiceRole.SPEAKER }
        val listeners = participants.filter { it.role == VoiceRole.LISTENER }

        return VoiceChannelStateDTO(
            channelId = channelId,
            sessionId = session.id,
            participantCount = participants.size,
            speakerCount = speakers.size,
            listenerCount = listeners.size,
            activeSpeakerIds = speakers.map { it.userId },
            roomName = session.roomName
        )
    }

    fun buildSessionState(channelId: String): VoiceStateResponse {
        val session = store.getActiveSession(channelId)
            ?: return VoiceStateResponse(null, emptyList(), 0, 0, emptyList())

        val participants = store.getActiveParticipants(session.id)
        val speakers = participants.filter { it.role == VoiceRole.SPEAKER }
        val listeners = participants.filter { it.role == VoiceRole.LISTENER }

        val sessionDTO = VoiceSessionDTO(
            id = session.id,
            channelId = session.channelId,
            roomName = session.roomName,
            status = session.status,
            createdAt = session.createdAt
        )

        return VoiceStateResponse(
            session = sessionDTO,
            participants = participants.map { toParticipantDTO(it) },
            speakerCount = speakers.size,
            listenerCount = listeners.size,
            activeSpeakerIds = speakers.map { it.userId }
        )
    }

    fun toParticipantDTO(p: VoiceParticipant): VoiceParticipantStateDTO {
        return VoiceParticipantStateDTO(
            participantId = p.id,
            userId = p.userId,
            channelId = p.channelId,
            sessionId = p.sessionId,
            role = p.role,
            isMicEnabled = p.isMicEnabled,
            isMuted = p.isMuted,
            isDeafened = p.isDeafened,
            connectionState = p.connectionState
        )
    }
}
