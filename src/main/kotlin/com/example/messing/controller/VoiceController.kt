package com.example.messing.controller

import com.example.messing.dto.voice.*
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
    // POST /api/voice/join
    // Client gọi khi muốn vào voice channel. Trả về token LiveKit + state.
    // Nếu wantMic = false hoặc user từ chối mic -> listenOnly = true, vẫn join được.
    @PostMapping("/join")
    fun joinVoice(@RequestBody request: JoinVoiceRequest, principal: Principal): JoinVoiceResponse {
        val user = userRepository.findByEmailOrUsername(principal.name, principal.name)
            ?: throw IllegalArgumentException("User not found")
        val userId = user.id ?: throw IllegalArgumentException("User id not found")

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

    // POST /api/voice/leave
    // Rời voice channel, backend cập nhật state và broadcast.
    @PostMapping("/leave")
    fun leaveVoice(@RequestBody request: LeaveVoiceRequest, principal: Principal): VoiceChannelStateDTO {
        val user = userRepository.findByEmailOrUsername(principal.name, principal.name)
            ?: throw IllegalArgumentException("User not found")
        val userId = user.id ?: throw IllegalArgumentException("User id not found")

        voiceParticipantService.markParticipantLeft(userId, request.sessionId)

        val participantsLeft = voiceParticipantService.getActiveParticipants(request.sessionId)
        if (participantsLeft.isEmpty()) {
            voiceSessionService.markSessionEmpty(request.sessionId)
        }

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)
        return channelState
    }

    // POST /api/voice/mic
    // Bật/tắt mic sau khi đã join. Cập nhật role speaker/listener tương ứng.
    @PostMapping("/mic")
    fun toggleMic(@RequestBody request: ToggleMicRequest, principal: Principal): VoiceParticipantStateDTO {
        val user = userRepository.findByEmailOrUsername(principal.name, principal.name)
            ?: throw IllegalArgumentException("User not found")
        val userId = user.id ?: throw IllegalArgumentException("User id not found")

        val participant = voiceParticipantService.markMicEnabled(userId, request.sessionId, request.enabled)
            ?: throw IllegalArgumentException("Participant state not found")

        val channelState = voiceChannelStateService.buildChannelState(request.channelId)
        voiceStateBroadcaster.broadcastStateChanged(channelState)

        return voiceChannelStateService.toParticipantDTO(participant)
    }

    // GET /api/voice/state/{channelId}
    // Lấy snapshot toàn bộ state của channel để render UI.
    @GetMapping("/state/{channelId}")
    fun getVoiceState(@PathVariable channelId: String): VoiceStateResponse {
        return voiceChannelStateService.buildSessionState(channelId)
    }
}
