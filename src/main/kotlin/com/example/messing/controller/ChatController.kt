package com.example.messing.controller

import com.example.messing.dto.message.ChatMessageRequest
import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.entity.Message
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.MessageRepository
import com.example.messing.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatController(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/chat/{channelId}/sendMessage")
    fun sendMessage(
        @DestinationVariable channelId: String,
        @Valid @Payload request: ChatMessageRequest,
        principal: Principal
    ) {
        val sender = userRepository.findByEmail(principal.name)
            ?: throw ResourceNotFoundException("User not found")

        val channel = channelRepository.findById(channelId).orElseThrow {
            ResourceNotFoundException("Channel not found")
        }

        val savedMessage = messageRepository.save(
            Message(
                content = request.content,
                sender = sender,
                channel = channel
            )
        )

        val response = ChatMessageResponse(
            id = savedMessage.id ?: throw IllegalStateException("Saved message id is null"),
            channelId = channel.id ?: channelId,
            content = savedMessage.content,
            createdAt = savedMessage.createdAt,
            senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
            senderUsername = sender.username,
            senderAvatarUrl = sender.avatarUrl
        )

        messagingTemplate.convertAndSend("/topic/channels/$channelId", response)
    }
}
