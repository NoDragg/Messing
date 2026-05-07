package com.example.messing.controller

import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.dto.message.UploadChatImageResponse
import com.example.messing.entity.Message
import com.example.messing.entity.MessageType
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.MessageRepository
import com.example.messing.repository.UserRepository
import com.example.messing.service.FileStorageService
import com.example.messing.service.MessageMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

@RestController
@RequestMapping("/api/channels")
class MessageRestController(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val fileStorageService: FileStorageService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val messageMapper: MessageMapper
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
            messageMapper.toResponse(message)
        }

        return ResponseEntity.ok(responsePage)
    }

    @PostMapping("/{channelId}/images", consumes = ["multipart/form-data"])
    fun uploadChannelImage(
        @PathVariable channelId: String,
        @RequestParam("file") file: MultipartFile,
        principal: Principal?
    ): ResponseEntity<UploadChatImageResponse> {
        val authPrincipal = principal ?: throw ResourceNotFoundException("Unauthorized")
        val sender = userRepository.findByEmailOrUsername(authPrincipal.name, authPrincipal.name)
            ?: throw ResourceNotFoundException("User not found")

        val channel = channelRepository.findById(channelId).orElseThrow {
            ResourceNotFoundException("Channel not found")
        }

        val imageUrl = fileStorageService.storeChatImage(file, "messages")

        val savedMessage = messageRepository.save(
            Message(
                content = imageUrl,
                type = MessageType.IMAGE,
                sender = sender,
                channel = channel
            )
        )

        val response = messageMapper.toResponse(savedMessage, sender, channelId)

        messagingTemplate.convertAndSend("/topic/channels/$channelId", response)

        return ResponseEntity.ok(UploadChatImageResponse(message = response))
    }
}
