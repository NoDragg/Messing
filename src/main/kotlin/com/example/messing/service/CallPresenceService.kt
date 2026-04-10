package com.example.messing.service

import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class CallPresenceService {

    private val onlineUsers = ConcurrentHashMap.newKeySet<String>()
    private val activeVoiceChannelByUser = ConcurrentHashMap<String, String>()
    private val voiceParticipantsByChannel = ConcurrentHashMap<String, MutableSet<String>>()
    private val voiceSessionByChannel = ConcurrentHashMap<String, String>()

    fun markOnline(userId: String?) {
        if (userId.isNullOrBlank()) return
        onlineUsers.add(userId)
    }

    fun markOffline(userId: String?): VoiceChannelChangeResult? {
        if (userId.isNullOrBlank()) return null
        onlineUsers.remove(userId)
        return leaveVoiceChannel(userId)
    }

    fun joinVoiceChannel(userId: String, channelId: String): VoiceChannelChangeResult {
        val previousChannelId = activeVoiceChannelByUser.put(userId, channelId)

        if (previousChannelId != null && previousChannelId != channelId) {
            leaveFromChannel(userId, previousChannelId)
        }

        voiceParticipantsByChannel.compute(channelId) { _, users ->
            val nextUsers = users ?: ConcurrentHashMap.newKeySet<String>()
            nextUsers.add(userId)
            nextUsers
        }

        val currentParticipants = getParticipants(channelId)
        val sessionId = voiceSessionByChannel.computeIfAbsent(channelId) { UUID.randomUUID().toString() }
        val isNewSession = currentParticipants.size == 1

        return VoiceChannelChangeResult(
            previousChannelId = if (previousChannelId == channelId) null else previousChannelId,
            currentChannelId = channelId,
            currentParticipantIds = currentParticipants,
            sessionId = sessionId,
            sessionStarted = isNewSession
        )
    }

    fun leaveVoiceChannel(userId: String): VoiceChannelChangeResult? {
        val currentChannelId = activeVoiceChannelByUser.remove(userId) ?: return null
        val stillInChannel = leaveFromChannel(userId, currentChannelId)
        val currentParticipants = if (stillInChannel) getParticipants(currentChannelId) else emptySet()

        if (!stillInChannel) {
            voiceSessionByChannel.remove(currentChannelId)
        }

        return VoiceChannelChangeResult(
            previousChannelId = currentChannelId,
            currentChannelId = if (stillInChannel) currentChannelId else null,
            currentParticipantIds = currentParticipants,
            sessionId = if (stillInChannel) voiceSessionByChannel[currentChannelId] else null,
            sessionStarted = false
        )
    }

    fun getParticipants(channelId: String): Set<String> {
        return voiceParticipantsByChannel[channelId]?.toSet() ?: emptySet()
    }

    fun getVoiceSessionId(channelId: String): String? {
        return voiceSessionByChannel[channelId]
    }

    fun getActiveVoiceChannelId(userId: String): String? {
        return activeVoiceChannelByUser[userId]
    }

    private fun leaveFromChannel(userId: String, channelId: String): Boolean {
        var hasParticipants = false

        voiceParticipantsByChannel.computeIfPresent(channelId) { _, users ->
            users.remove(userId)
            hasParticipants = users.isNotEmpty()
            if (users.isEmpty()) null else users
        }

        if (!hasParticipants) {
            voiceSessionByChannel.remove(channelId)
        }

        return hasParticipants
    }
}

data class VoiceChannelChangeResult(
    val previousChannelId: String?,
    val currentChannelId: String?,
    val currentParticipantIds: Set<String>,
    val sessionId: String?,
    val sessionStarted: Boolean
)
