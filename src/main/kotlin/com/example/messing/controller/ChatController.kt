package com.example.messing.controller

import com.example.messing.dto.message.ChatMessageRequest
import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.entity.Message
import com.example.messing.entity.MessageType
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.MessageRepository
import com.example.messing.repository.UserRepository
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatController(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @MessageMapping("/chat/{channelId}/sendMessage")
    fun sendMessage(
        @DestinationVariable channelId: String,
        @Valid @Payload request: ChatMessageRequest,
        principal: Principal
    ) {
        logger.info(
            "Incoming websocket chat message channelId={} principal={} contentLength={}",
            channelId,
            principal.name,
            request.content.length
        )

        val principalName = principal.name

        val sender = userRepository.findByEmail(principalName)
            ?: throw ResourceNotFoundException("Sender not found for authenticated principal: $principalName")

        val channel = channelRepository.findById(channelId).orElseThrow {
            ResourceNotFoundException("Channel not found")
        }

        val savedMessage = messageRepository.save(
            Message(
                content = request.content,
                type = MessageType.TEXT,
                sender = sender,
                channel = channel
            )
        )

        val response = ChatMessageResponse(
            id = savedMessage.id ?: throw IllegalStateException("Saved message id is null"),
            channelId = channel.id ?: channelId,
            content = savedMessage.content,
            type = savedMessage.type,
            createdAt = savedMessage.createdAt,
            senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
            senderUsername = sender.username,
            senderAvatarUrl = sender.avatarUrl
        )

        logger.info(
            "Saved websocket chat message id={} channelId={} sender={} contentLength={}",
            response.id,
            response.channelId,
            response.senderUsername,
            response.content.length
        )

        messagingTemplate.convertAndSend("/topic/channels/$channelId", response)
    }
}
