package com.example.messing.controller

import com.example.messing.dto.channel.ChannelResponse
import com.example.messing.dto.channel.CreateChannelRequest
import com.example.messing.service.ChannelService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/servers/{serverId}/channels")
class ChannelController(
    private val channelService: ChannelService
) {

    @PostMapping
    fun createChannel(
        @PathVariable serverId: String,
        @Valid @RequestBody request: CreateChannelRequest,
        authentication: Authentication
    ): ResponseEntity<ChannelResponse> {
        val response = channelService.createChannel(serverId, request, authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getChannels(
        @PathVariable serverId: String,
        authentication: Authentication
    ): ResponseEntity<List<ChannelResponse>> {
        val channels = channelService.getChannelsByServer(serverId, authentication.name)
        return ResponseEntity.ok(channels)
    }
}
