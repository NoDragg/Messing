package com.example.messing.service

import com.example.messing.exception.BadRequestException
import com.example.messing.exception.ResourceNotFoundException

fun channelNotFound(message: String = "Channel not found"): Nothing {
    throw ResourceNotFoundException(message)
}

fun voiceChannelOnly(message: String = "Channel is not a voice channel"): Nothing {
    throw BadRequestException(message)
}
