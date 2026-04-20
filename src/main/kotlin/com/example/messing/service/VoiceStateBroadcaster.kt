package com.example.messing.service

import com.example.messing.dto.voice.VoiceChannelStateDTO
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class VoiceStateBroadcaster(
    private val messagingTemplate: SimpMessagingTemplate
) {
    // Broadcast toàn bộ trạng thái voice channel tới các client đang subscribe
    fun broadcastStateChanged(stateDTO: VoiceChannelStateDTO) {
        messagingTemplate.convertAndSend("/topic/voice/${stateDTO.channelId}", stateDTO)
    }
}
