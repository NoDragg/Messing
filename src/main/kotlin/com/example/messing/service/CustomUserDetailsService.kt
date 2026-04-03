package com.example.messing.service

import com.example.messing.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(identifier: String): UserDetails {
        val user = userRepository.findByEmailOrLoginName(identifier, identifier)
            ?: throw UsernameNotFoundException("User not found with identifier: $identifier")

        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()
    }
}
