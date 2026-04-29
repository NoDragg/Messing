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
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email already exists")
        }
        if (userRepository.existsByUsername(request.username)) {
            throw BadRequestException("Username already exists")
        }

        val user = User(
            username = request.username,
            displayName = request.displayName,
            email = request.email,
            password = passwordEncoder.encode(request.password) ?: ""
        )
        val savedUser = userRepository.save(user)

        val userDetails: UserDetails = userDetailsService.loadUserByUsername(savedUser.username)
        val token = jwtUtil.generateToken(userDetails)

        return AuthResponse(
            token = token,
            userId = savedUser.id!!,
            username = savedUser.username,
            displayName = savedUser.displayName?.takeIf { it.isNotBlank() },
            email = savedUser.email
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.identifier, request.password)
        )

        val user = userRepository.findByEmailOrUsername(request.identifier, request.identifier)
            ?: throw BadRequestException("Invalid credentials")

        if (user.isVirtual) {
            throw BadRequestException("Invalid credentials")
        }

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
