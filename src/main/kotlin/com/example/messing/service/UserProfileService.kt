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
    fun getMyProfile(currentUserEmail: String): UserProfileResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            loginName = user.loginName,
            email = user.email,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            createdAt = user.createdAt
        )
    }

    @Transactional
    fun updateMyProfile(request: UpdateProfileRequest, currentUserEmail: String): UserProfileResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        request.username?.let { user.username = it }
        request.bio?.let { user.bio = it }

        userRepository.save(user)

        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            loginName = user.loginName,
            email = user.email,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            createdAt = user.createdAt
        )
    }

    @Transactional
    fun updateMyAvatar(file: MultipartFile, currentUserEmail: String): String {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        val avatarUrl = fileStorageService.storeCircularAvatar(file, "users")
        user.avatarUrl = avatarUrl
        userRepository.save(user)

        return avatarUrl
    }
}
