package com.example.messing.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class VoicePresenceService {
    private val onlineUsers = ConcurrentHashMap.newKeySet<String>()

    fun markOnline(userId: String?) {
        if (userId.isNullOrBlank()) return
        onlineUsers.add(userId)
    }

    fun markOffline(userId: String?) {
        if (userId.isNullOrBlank()) return
        onlineUsers.remove(userId)
    }

    fun isOnline(userId: String?): Boolean {
        if (userId.isNullOrBlank()) return false
        return onlineUsers.contains(userId)
    }
}
