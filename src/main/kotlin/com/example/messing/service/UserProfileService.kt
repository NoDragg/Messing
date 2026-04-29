package com.example.messing.service

import com.example.messing.dto.user.UpdateProfileRequest
import com.example.messing.dto.user.UserProfileResponse
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserProfileService(
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {


    @Transactional(readOnly = true)
    fun getMyProfile(currentUserIdentifier: String): UserProfileResponse {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.username,
            email = user.email,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            createdAt = user.createdAt
        )
    }


    @Transactional
    fun updateMyProfile(request: UpdateProfileRequest, currentUserIdentifier: String): UserProfileResponse {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        user.displayName = request.displayName?.takeIf { it.isNotBlank() }
        user.bio = request.bio?.let { it }

        userRepository.save(user)

        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.username,
            email = user.email,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            createdAt = user.createdAt
        )
    }

    @Transactional
    fun updateMyAvatar(file: MultipartFile, currentUserIdentifier: String): String {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val avatarUrl = fileStorageService.storeCircularAvatar(file, "users")
        user.avatarUrl = avatarUrl
        userRepository.save(user)

        return avatarUrl
    }
}
