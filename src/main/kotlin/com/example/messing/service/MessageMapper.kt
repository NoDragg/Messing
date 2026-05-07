package com.example.messing.service

import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.entity.Message
import com.example.messing.entity.User
import com.example.messing.exception.ResourceNotFoundException
import org.springframework.stereotype.Component

@Component
class MessageMapper {
    fun toResponse(message: Message): ChatMessageResponse {
        val sender = message.sender ?: throw ResourceNotFoundException("Sender not found for message ${message.id}")
        val channel = message.channel ?: throw ResourceNotFoundException("Channel not found for message ${message.id}")

        return ChatMessageResponse(
            id = message.id ?: throw IllegalStateException("Message id is null"),
            channelId = channel.id ?: throw IllegalStateException("Channel id is null"),
            content = message.content,
            type = message.type,
            createdAt = message.createdAt,
            senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
            senderUsername = sender.username,
            senderDisplayName = sender.displayName?.takeIf { it.isNotBlank() } ?: sender.username,
            senderAvatarUrl = sender.avatarUrl,
            metadata = null
        )
    }

    fun toResponse(message: Message, sender: User, channelIdFallback: String): ChatMessageResponse {
        return ChatMessageResponse(
            id = message.id ?: throw IllegalStateException("Saved message id is null"),
            channelId = message.channel?.id ?: channelIdFallback,
            content = message.content,
            type = message.type,
            createdAt = message.createdAt,
            senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
            senderUsername = sender.username,
            senderDisplayName = sender.displayName?.takeIf { it.isNotBlank() } ?: sender.username,
            senderAvatarUrl = sender.avatarUrl,
            metadata = null
        )
    }
}
