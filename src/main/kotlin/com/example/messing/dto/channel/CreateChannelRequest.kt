package com.example.messing.dto.channel

import com.example.messing.entity.ChannelType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChannelRequest(
    @field:NotBlank(message = "Channel name is required")
    @field:Size(min = 1, max = 100, message = "Channel name must be between 1 and 100 characters")
    val name: String,

    val type: ChannelType = ChannelType.TEXT
)
