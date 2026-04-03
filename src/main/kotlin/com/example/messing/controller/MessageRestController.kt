package com.example.messing.controller

import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.MessageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/channels")
class MessageRestController(
    private val messageRepository: MessageRepository
) {

    @GetMapping("/{channelId}/messages")
    fun getChannelMessages(
        @PathVariable channelId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Page<ChatMessageResponse>> {
        val pageable = PageRequest.of(page, size)
        val messagesPage = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)

        val responsePage = messagesPage.map { message ->
            val sender = message.sender ?: throw ResourceNotFoundException("Sender not found for message ${message.id}")
            val channel = message.channel ?: throw ResourceNotFoundException("Channel not found for message ${message.id}")

            ChatMessageResponse(
                id = message.id ?: throw IllegalStateException("Message id is null"),
                channelId = channel.id ?: channelId,
                content = message.content,
                createdAt = message.createdAt,
                senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
                senderUsername = sender.username,
                senderAvatarUrl = sender.avatarUrl
            )
        }

        return ResponseEntity.ok(responsePage)
    }
}
