package com.example.messing.service

import com.example.messing.entity.ChannelType
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.ServerMemberRepository
import org.springframework.stereotype.Service

@Service
class VoiceAuthorizationService(
    private val channelRepository: ChannelRepository,
    private val serverMemberRepository: ServerMemberRepository
) {
    fun assertCanJoinVoice(userId: String, channelId: String) {
        val channel = channelRepository.findById(channelId).orElseThrow {
            IllegalArgumentException("Channel not found")
        }

        if (channel.type != ChannelType.VOICE) {
            throw IllegalArgumentException("Channel is not a voice channel")
        }

        val serverId = channel.server?.id
            ?: throw IllegalArgumentException("Server not found")

        if (!serverMemberRepository.existsByUserIdAndServerId(userId, serverId)) {
            throw IllegalArgumentException("You do not have permission to join this voice channel")
        }
    }
}
