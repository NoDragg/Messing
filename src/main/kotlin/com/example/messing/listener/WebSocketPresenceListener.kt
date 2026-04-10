package com.example.messing.listener

import com.example.messing.repository.UserRepository
import com.example.messing.service.CallPresenceService
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketPresenceListener(
    private val userRepository: UserRepository,
    private val callPresenceService: CallPresenceService
) {

    @EventListener
    fun onConnect(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val principalName = accessor.user?.name ?: return

        val user = userRepository.findByEmail(principalName) ?: return
        callPresenceService.markOnline(user.id)
    }

    @EventListener
    fun onDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val principalName = accessor.user?.name ?: return

        val user = userRepository.findByEmail(principalName) ?: return
        callPresenceService.markOffline(user.id)
    }
}
