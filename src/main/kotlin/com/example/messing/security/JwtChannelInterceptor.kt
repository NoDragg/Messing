package com.example.messing.security

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

    companion object {
        private const val SESSION_USER_KEY = "AUTHENTICATED_STOMP_USER"
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command != StompCommand.CONNECT) {
            val sessionUser = accessor.sessionAttributes?.get(SESSION_USER_KEY)
            if (sessionUser is UsernamePasswordAuthenticationToken && accessor.user == null) {
                accessor.user = sessionUser
            }

            return message
        }

        val rawAuthHeader = accessor.getFirstNativeHeader("X-Authorization")
            ?: accessor.getFirstNativeHeader("Authorization") ?: return message

        val token = rawAuthHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            return message
        }

        return try {
            val email = jwtUtil.extractEmail(token) ?: return message
            val userDetails = customUserDetailsService.loadUserByUsername(email)

            if (!jwtUtil.isTokenValid(token, userDetails)) {
                return message
            }

            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            accessor.user = authentication
            accessor.sessionAttributes?.put(SESSION_USER_KEY, authentication)

            message
        } catch (_: Exception) {
            null
        }
    }
}
