package com.example.messing.service

import com.example.messing.dto.channel.ChannelResponse
import com.example.messing.dto.channel.CreateChannelRequest
import com.example.messing.entity.Channel
import com.example.messing.exception.BadRequestException
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.ServerMemberRepository
import com.example.messing.repository.ServerRepository
import com.example.messing.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createChannel(
        serverId: String,
        request: CreateChannelRequest,
        currentUserEmail: String
    ): ChannelResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        val server = serverRepository.findById(serverId)
            .orElseThrow { ResourceNotFoundException("Server not found") }

        // Verify the user is a member of this server
        if (!serverMemberRepository.existsByUserIdAndServerId(user.id!!, serverId)) {
            throw BadRequestException("You are not a member of this server")
        }

        // Verify channel name uniqueness in this server
        if (channelRepository.existsByServerIdAndName(serverId, request.name)) {
            throw BadRequestException("A channel with the name '${request.name}' already exists in this server")
        }

        val channel = Channel(
            name = request.name,
            type = request.type,
            server = server
        )
        val savedChannel = channelRepository.save(channel)

        return ChannelResponse.from(savedChannel)
    }

    fun getChannelsByServer(serverId: String, currentUserEmail: String): List<ChannelResponse> {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        // Verify the server exists
        if (!serverRepository.existsById(serverId)) {
            throw ResourceNotFoundException("Server not found")
        }

        // Verify the user is a member
        if (!serverMemberRepository.existsByUserIdAndServerId(user.id!!, serverId)) {
            throw BadRequestException("You are not a member of this server")
        }

        val channels = channelRepository.findAllByServerId(serverId)
        return channels.map { ChannelResponse.from(it) }
    }
}
