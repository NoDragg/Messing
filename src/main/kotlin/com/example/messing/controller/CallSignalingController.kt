package com.example.messing.controller

import com.example.messing.dto.call.CallSignalMessage
import com.example.messing.dto.call.CallSignalRequest
import com.example.messing.dto.call.CallSignalType
import com.example.messing.entity.ChannelType
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.ServerMemberRepository
import com.example.messing.repository.UserRepository
import com.example.messing.service.CallPresenceService
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class CallSignalingController(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val callPresenceService: CallPresenceService
) {

    @MessageMapping("/call/signal")
    fun signal(
        @Valid @Payload request: CallSignalRequest,
        principal: Principal
    ) {
        val fromUser = userRepository.findByEmail(principal.name)
            ?: return

        val fromUserId = fromUser.id ?: return

        when (request.type) {
            CallSignalType.VOICE_JOIN -> handleVoiceJoin(request, fromUserId, fromUser.username)
            CallSignalType.VOICE_LEAVE -> handleVoiceLeave(fromUserId, fromUser.username)
            CallSignalType.VOICE_OFFER,
            CallSignalType.VOICE_ANSWER,
            CallSignalType.VOICE_ICE -> handleVoicePeerSignal(request, fromUserId, fromUser.username)
            CallSignalType.VOICE_PARTICIPANTS,
            CallSignalType.VOICE_ERROR -> {
                // server-only signal types
            }
        }
    }

    private fun handleVoiceJoin(request: CallSignalRequest, fromUserId: String, fromUsername: String) {
        val channelId = request.roomId
        val channel = channelRepository.findById(channelId).orElse(null)

        if (channel == null) {
            sendVoiceError(
                userId = fromUserId,
                roomId = channelId,
                fromUsername = fromUsername,
                reason = "CHANNEL_NOT_FOUND",
                message = "Voice channel không tồn tại"
            )
            return
        }

        if (channel.type != ChannelType.VOICE) {
            sendVoiceError(
                userId = fromUserId,
                roomId = channelId,
                fromUsername = fromUsername,
                reason = "CHANNEL_NOT_VOICE",
                message = "Channel hiện tại không phải voice channel"
            )
            return
        }

        val serverId = channel.server?.id
        if (serverId.isNullOrBlank() || !serverMemberRepository.existsByUserIdAndServerId(fromUserId, serverId)) {
            sendVoiceError(
                userId = fromUserId,
                roomId = channelId,
                fromUsername = fromUsername,
                reason = "NOT_A_MEMBER",
                message = "Bạn không có quyền vào voice channel này"
            )
            return
        }

        val joinResult = callPresenceService.joinVoiceChannel(fromUserId, channelId)

        joinResult.previousChannelId
            ?.takeIf { it != channelId }
            ?.let { previousChannelId ->
                broadcastVoiceParticipants(previousChannelId)
            }

        broadcastVoiceParticipants(channelId)
    }

    private fun handleVoiceLeave(fromUserId: String, fromUsername: String) {
        val leaveResult = callPresenceService.leaveVoiceChannel(fromUserId) ?: return

        val previousChannelId = leaveResult.previousChannelId
        if (previousChannelId.isNullOrBlank()) {
            return
        }

        broadcastVoiceParticipants(previousChannelId)

        val leftAck = CallSignalMessage(
            fromUserId = fromUserId,
            fromUsername = fromUsername,
            toUserId = fromUserId,
            roomId = previousChannelId,
            type = CallSignalType.VOICE_LEAVE,
            metadata = mapOf("left" to true)
        )

        sendToUser(fromUserId, leftAck)
    }

    private fun handleVoicePeerSignal(request: CallSignalRequest, fromUserId: String, fromUsername: String) {
        if (request.toUserId == fromUserId) return

        val fromActiveChannel = callPresenceService.getActiveVoiceChannelId(fromUserId)
        val toActiveChannel = callPresenceService.getActiveVoiceChannelId(request.toUserId)
        val requestedChannel = request.roomId

        if (fromActiveChannel != requestedChannel || toActiveChannel != requestedChannel) {
            sendVoiceError(
                userId = fromUserId,
                roomId = requestedChannel,
                fromUsername = fromUsername,
                reason = "PEER_NOT_IN_SAME_CHANNEL",
                message = "Peer không còn trong cùng voice channel"
            )
            return
        }

        val signalMessage = buildSignalMessage(
            fromUserId = fromUserId,
            fromUsername = fromUsername,
            request = request
        )

        sendToUser(request.toUserId, signalMessage)
    }

    private fun buildSignalMessage(
        fromUserId: String,
        fromUsername: String,
        request: CallSignalRequest
    ): CallSignalMessage {
        return CallSignalMessage(
            fromUserId = fromUserId,
            fromUsername = fromUsername,
            toUserId = request.toUserId,
            roomId = request.roomId,
            type = request.type,
            sdp = request.sdp,
            candidate = request.candidate,
            sdpMid = request.sdpMid,
            sdpMLineIndex = request.sdpMLineIndex,
            metadata = request.metadata
        )
    }

    private fun broadcastVoiceParticipants(channelId: String) {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return
        val serverId = channel.server?.id ?: return

        val participantIds = callPresenceService.getParticipants(channelId)
        val sessionId = callPresenceService.getVoiceSessionId(channelId)

        val participants = if (participantIds.isEmpty()) {
            emptyList<Map<String, String?>>()
        } else {
            userRepository.findAllById(participantIds)
                .mapNotNull { user ->
                    val id = user.id ?: return@mapNotNull null
                    mapOf(
                        "id" to id,
                        "username" to user.username,
                        "avatarUrl" to user.avatarUrl
                    )
                }
        }

        val serverMemberUserIds = serverMemberRepository.findAllByServerId(serverId)
            .mapNotNull { it.user?.id }
            .toSet()

        serverMemberUserIds.forEach { memberUserId ->
            val participantsMessage = CallSignalMessage(
                fromUserId = memberUserId,
                fromUsername = "voice-system",
                toUserId = memberUserId,
                roomId = channelId,
                type = CallSignalType.VOICE_PARTICIPANTS,
                metadata = mapOf(
                    "channelId" to channelId,
                    "users" to participants,
                    "sessionId" to sessionId,
                    "isNewSession" to (participants.size == 1)
                )
            )

            sendToUser(memberUserId, participantsMessage)
        }
    }

    private fun sendVoiceError(
        userId: String,
        roomId: String,
        fromUsername: String,
        reason: String,
        message: String
    ) {
        val errorMessage = CallSignalMessage(
            fromUserId = userId,
            fromUsername = fromUsername,
            toUserId = userId,
            roomId = roomId,
            type = CallSignalType.VOICE_ERROR,
            metadata = mapOf(
                "reason" to reason,
                "message" to message
            )
        )

        sendToUser(userId, errorMessage)
    }

    private fun sendToUser(userId: String, message: CallSignalMessage) {
        messagingTemplate.convertAndSend(
            "/queue/call-signals/$userId",
            message
        )
    }
}
