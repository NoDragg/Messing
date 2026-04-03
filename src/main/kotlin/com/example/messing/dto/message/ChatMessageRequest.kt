package com.example.messing.dto.message

import jakarta.validation.constraints.NotBlank

data class ChatMessageRequest(
    @field:NotBlank(message = "Message content is required")
    val content: String
)
