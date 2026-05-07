package com.example.messing.service

import com.example.messing.entity.User
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CurrentUserResolver(
    private val userRepository: UserRepository
) {
    fun resolve(identifier: String): User {
        return userRepository.findByEmailOrUsername(identifier, identifier)
            ?: throw ResourceNotFoundException("User not found")
    }
}
