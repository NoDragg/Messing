package com.example.messing.service

import com.example.messing.security.JwtUtil
import com.example.messing.dto.auth.AuthResponse
import com.example.messing.dto.auth.LoginRequest
import com.example.messing.dto.auth.RegisterRequest
import com.example.messing.entity.User
import com.example.messing.exception.BadRequestException
import com.example.messing.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService
) {

    fun register(request: RegisterRequest): AuthResponse {
        ensureUniqueRegistration(request.email, request.username)

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password.trim())) {
            "Password encoding failed"
        }
        val user = User(
            username = request.username.trim(),
            displayName = request.displayName?.trim().takeIf { !it.isNullOrBlank() },
            email = request.email.trim(),
            password = encodedPassword
        )
        val savedUser = userRepository.save(user)

        return buildAuthResponse(savedUser)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.identifier, request.password)
        )

        val user = userRepository.findByEmailOrUsername(request.identifier, request.identifier)
            ?: throw invalidCredentials()

        if (user.isVirtual) {
            throw invalidCredentials()
        }

        return buildAuthResponse(user)
    }

    private fun ensureUniqueRegistration(email: String, username: String) {
        if (userRepository.existsByEmail(email)) {
            throw BadRequestException("Email already exists")
        }
        if (userRepository.existsByUsername(username)) {
            throw BadRequestException("Username already exists")
        }
    }

    private fun invalidCredentials(): BadRequestException {
        return BadRequestException("Invalid credentials")
    }

    private fun buildAuthResponse(user: User): AuthResponse {
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(user.username)
        val token = jwtUtil.generateToken(userDetails)

        return AuthResponse(
            token = token,
            userId = user.id!!,
            username = user.username,
            displayName = user.displayName?.takeIf { it.isNotBlank() },
            email = user.email
        )
    }
}
