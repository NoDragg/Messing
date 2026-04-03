package com.example.messing.security

import com.example.messing.config.JwtUtil
import com.example.messing.service.CustomUserDetailsService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class JwtChannelInterceptor(
    private val jwtUtil: JwtUtil,
    private val customUserDetailsService: CustomUserDetailsService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command != StompCommand.CONNECT) {
            return message
        }

        val rawAuthHeader = accessor.getFirstNativeHeader("X-Authorization")
            ?: accessor.getFirstNativeHeader("Authorization")
            ?: return message

        val token = rawAuthHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            return message
        }

        try {
            val email = jwtUtil.extractEmail(token) ?: return message
            val userDetails = customUserDetailsService.loadUserByUsername(email)

            if (jwtUtil.isTokenValid(token, userDetails)) {
                accessor.user = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
            }
        } catch (_: Exception) {
            // Invalid token: keep unauthenticated for downstream handling
        }

        return message
    }
}
