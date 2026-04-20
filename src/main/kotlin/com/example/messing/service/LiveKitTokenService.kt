package com.example.messing.service

import com.example.messing.config.LiveKitProperties
import com.example.messing.dto.voice.VoiceRole
import io.livekit.server.AccessToken
import io.livekit.server.CanPublish
import io.livekit.server.CanPublishData
import io.livekit.server.CanSubscribe
import io.livekit.server.RoomJoin
import io.livekit.server.RoomName
import org.springframework.stereotype.Service

@Service
class LiveKitTokenService(
    private val liveKitProperties: LiveKitProperties
) {
    fun createJoinToken(userId: String, username: String, roomName: String, role: VoiceRole, listenOnly: Boolean): String {
        val accessToken = AccessToken(
            liveKitProperties.apiKey,
            liveKitProperties.apiSecret,
        )
        accessToken.identity = userId
        accessToken.name = username
        accessToken.metadata = buildString {
            append('{')
            append("\"userId\":\"").append(userId).append("\",")
            append("\"username\":\"").append(username).append("\",")
            append("\"roomId\":\"").append(roomName).append("\",")
            append("\"role\":\"").append(role.name).append("\",")
            append("\"listenOnly\":").append(listenOnly)
            append('}')
        }

        val canPublish = !listenOnly && role == VoiceRole.SPEAKER

        accessToken.addGrants(
            RoomJoin(true),
            RoomName(roomName),
            CanPublish(canPublish),
            CanSubscribe(true),
            CanPublishData(true),
        )

        return accessToken.toJwt()
    }

    fun getUrl(): String = liveKitProperties.url
}
