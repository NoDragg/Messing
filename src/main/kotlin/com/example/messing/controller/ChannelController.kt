package com.example.messing.controller

import com.example.messing.dto.channel.ChannelResponse
import com.example.messing.dto.channel.CreateChannelRequest
import com.example.messing.dto.channel.UpdateChannelRequest
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

    @PutMapping("/{channelId}")
    fun renameChannel(
        @PathVariable serverId: String,
        @PathVariable channelId: String,
        @Valid @RequestBody request: UpdateChannelRequest,
        authentication: Authentication
    ): ResponseEntity<ChannelResponse> {
        val response = channelService.renameChannel(serverId, channelId, request, authentication.name)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{channelId}")
    fun deleteChannel(
        @PathVariable serverId: String,
        @PathVariable channelId: String,
        authentication: Authentication
    ): ResponseEntity<Void> {
        channelService.deleteChannel(serverId, channelId, authentication.name)
        return ResponseEntity.noContent().build()
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
