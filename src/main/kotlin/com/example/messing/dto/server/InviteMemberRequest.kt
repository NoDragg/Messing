package com.example.messing.dto.server

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class InviteMemberRequest(
    @field:NotBlank(message = "email is required")
    @field:Email(message = "email must be valid")
    val email: String
)
