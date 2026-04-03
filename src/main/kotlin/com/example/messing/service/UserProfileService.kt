package com.example.messing.service

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
