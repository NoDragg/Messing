package com.example.messing.controller

import com.example.messing.dto.UploadImageResponse
import com.example.messing.service.UserProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userProfileService: UserProfileService
) {

    @PostMapping("/me/avatar", consumes = ["multipart/form-data"])
    fun updateMyAvatar(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<UploadImageResponse> {
        val url = userProfileService.updateMyAvatar(file, authentication.name)
        return ResponseEntity.ok(UploadImageResponse(url = url))
    }
}
