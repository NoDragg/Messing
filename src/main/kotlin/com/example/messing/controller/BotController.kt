package com.example.messing.controller

import com.example.messing.dto.bot.BotChatRequest
import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.service.BotChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/bot")
class BotController(
    private val botChatService: BotChatService
) {

    @PostMapping("/chat")
    fun chat(
        @RequestBody request: BotChatRequest,
        principal: Principal
    ): ResponseEntity<ChatMessageResponse> {
        val answer = botChatService.generateAnswer(request, principal.name)
        return ResponseEntity.ok(answer)
    }
}
