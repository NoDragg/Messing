package com.example.messing.controller

import com.example.messing.dto.UploadImageResponse
import com.example.messing.dto.user.UpdateProfileRequest
import com.example.messing.dto.user.UserProfileResponse
import com.example.messing.service.UserProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userProfileService: UserProfileService
) {

    @GetMapping("/me/profile")
    fun getMyProfile(authentication: Authentication): ResponseEntity<UserProfileResponse> {
        val profile = userProfileService.getMyProfile(authentication.name)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/me/profile")
    fun updateMyProfile(
        @RequestBody request: UpdateProfileRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileResponse> {
        val profile = userProfileService.updateMyProfile(request, authentication.name)
        return ResponseEntity.ok(profile)
    }

    @PostMapping("/me/avatar", consumes = ["multipart/form-data"])
    fun updateMyAvatar(
        @RequestParam("avatar") avatar: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<UploadImageResponse> {
        val url = userProfileService.updateMyAvatar(avatar, authentication.name)
        return ResponseEntity.ok(UploadImageResponse(url = url))
    }
}
