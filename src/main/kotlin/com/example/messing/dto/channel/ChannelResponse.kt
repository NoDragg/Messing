package com.example.messing.dto.channel

import com.example.messing.entity.Channel
import com.example.messing.entity.ChannelType
import java.time.Instant

data class ChannelResponse(
    val id: String,
    val name: String,
    val type: ChannelType,
    val serverId: String,
    val createdAt: Instant
) {
    companion object {
        fun from(channel: Channel): ChannelResponse {
            return ChannelResponse(
                id = channel.id!!,
                name = channel.name,
                type = channel.type,
                serverId = channel.server?.id ?: "",
                createdAt = channel.createdAt
            )
        }
    }
}
