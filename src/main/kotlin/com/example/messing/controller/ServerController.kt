package com.example.messing.controller

import com.example.messing.dto.UploadImageResponse
import com.example.messing.dto.server.CreateInviteResponse
import com.example.messing.dto.server.CreateServerRequest
import com.example.messing.dto.server.InviteAcceptResponse
import com.example.messing.dto.server.InviteMemberRequest
import com.example.messing.dto.server.ServerResponse
import com.example.messing.service.ServerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/servers")
class ServerController(
    private val serverService: ServerService
) {

    @PostMapping
    fun createServer(
        @Valid @RequestBody request: CreateServerRequest,
        authentication: Authentication
    ): ResponseEntity<ServerResponse> {
        val response = serverService.createServer(request, authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{serverId}/invite")
    fun inviteMember(
        @PathVariable serverId: String,
        @Valid @RequestBody request: InviteMemberRequest,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        serverService.inviteMember(serverId, request, authentication.name)
        return ResponseEntity.ok(mapOf("message" to "Mời thành viên thành công"))
    }

    @PostMapping("/{serverId}/invites")
    fun createInviteLink(
        @PathVariable serverId: String,
        authentication: Authentication
    ): ResponseEntity<CreateInviteResponse> {
        val response = serverService.createInviteLink(serverId, authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/invites/{code}/accept")
    fun acceptInvite(
        @PathVariable code: String,
        authentication: Authentication
    ): ResponseEntity<InviteAcceptResponse> {
        val response = serverService.joinServerByInviteCode(code, authentication.name)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getMyServers(authentication: Authentication): ResponseEntity<List<ServerResponse>> {
        val servers = serverService.getServersForUser(authentication.name)
        return ResponseEntity.ok(servers)
    }

    @PostMapping("/{serverId}/avatar", consumes = ["multipart/form-data"])
    fun updateServerAvatar(
        @PathVariable serverId: String,
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<UploadImageResponse> {
        val url = serverService.updateServerAvatar(serverId, file, authentication.name)
        return ResponseEntity.ok(UploadImageResponse(url = url))
    }
}
