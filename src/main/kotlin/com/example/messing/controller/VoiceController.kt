package com.example.messing.controller

import com.example.messing.dto.voice.*
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.UserRepository
import com.example.messing.service.*
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/voice")
class VoiceController(
    private val userRepository: UserRepository,
    private val voiceAuthorizationService: VoiceAuthorizationService,
    private val voiceSessionService: VoiceSessionService,
    private val voiceParticipantService: VoiceParticipantService,
    private val liveKitTokenService: LiveKitTokenService,
    private val voiceChannelStateService: VoiceChannelStateService,
    private val voiceStateBroadcaster: VoiceStateBroadcaster
) {
    @PostMapping("/join")
    fun joinVoice(@RequestBody request: JoinVoiceRequest, principal: Principal): JoinVoiceResponse {
        val user = resolveCurrentUser(principal)
        val userId = user.id ?: throw ResourceNotFoundException("User id not found")

        voiceAuthorizationService.assertCanJoinVoice(userId, request.channelId)

        val session = voiceSessionService.getOrCreateActiveSession(request.channelId)
        val participant = voiceParticipantService.upsertJoinState(
            userId = userId,
            channelId = request.channelId,
            sessionId = session.id,
            listenOnly = !request.wantMic
        )

        val token = liveKitTokenService.createJoinToken(
            userId = userId,
            username = user.username,
            avatarUrl = user.avatarUrl,
            roomName = session.roomName,
            role = participant.role,
            listenOnly = !participant.isMicEnabled
        )

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)

        return JoinVoiceResponse(
            sessionId = session.id,
            channelId = request.channelId,
            roomName = session.roomName,
            livekitUrl = liveKitTokenService.getUrl(),
            livekitToken = token,
            listenOnly = !participant.isMicEnabled,
            participantState = voiceChannelStateService.toParticipantDTO(participant),
            channelState = channelState
        )
    }

    @PostMapping("/leave")
    fun leaveVoice(@RequestBody request: LeaveVoiceRequest, principal: Principal): VoiceChannelStateDTO {
        val user = resolveCurrentUser(principal)
        val userId = user.id ?: throw ResourceNotFoundException("User id not found")

        voiceParticipantService.markParticipantLeft(userId, request.sessionId)

        val participantsLeft = voiceParticipantService.getActiveParticipants(request.sessionId)
        if (participantsLeft.isEmpty()) {
            voiceSessionService.markSessionEmpty(request.sessionId)
        }

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)
        return channelState
    }

    @PostMapping("/mic")
    fun toggleMic(@RequestBody request: ToggleMicRequest, principal: Principal): VoiceParticipantStateDTO {
        val user = resolveCurrentUser(principal)
        val userId = user.id ?: throw ResourceNotFoundException("User id not found")

        val participant = voiceParticipantService.markMicEnabled(userId, request.sessionId, request.enabled)
            ?: throw ResourceNotFoundException("Participant state not found")

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)

        return voiceChannelStateService.toParticipantDTO(participant)
    }

    @PostMapping("/screen-share")
    fun toggleScreenShare(@RequestBody request: ScreenShareRequest, principal: Principal): VoiceParticipantStateDTO {
        val user = resolveCurrentUser(principal)
        val userId = user.id ?: throw ResourceNotFoundException("User id not found")

        voiceAuthorizationService.assertCanJoinVoice(userId, request.channelId)

        val participant = voiceParticipantService.markScreenShareEnabled(
            userId = userId,
            sessionId = request.sessionId,
            enabled = request.enabled,
            trackSid = request.trackSid,
            source = request.source,
        ) ?: throw ResourceNotFoundException("Participant state not found")

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)

        return voiceChannelStateService.toParticipantDTO(participant)
    }

    @GetMapping("/state/{channelId}")
    fun getVoiceState(@PathVariable channelId: String): VoiceStateResponse {
        return voiceChannelStateService.buildSessionState(channelId)
    }

    private fun resolveCurrentUser(principal: Principal) =
        userRepository.findByEmailOrUsername(principal.name, principal.name)
            ?: throw ResourceNotFoundException("User not found")
}
