package com.example.messing.service

import com.example.messing.dto.channel.ChannelResponse
import com.example.messing.dto.channel.CreateChannelRequest
import com.example.messing.dto.channel.UpdateChannelRequest
import com.example.messing.entity.Channel
import com.example.messing.entity.MemberRole
import com.example.messing.exception.BadRequestException
import com.example.messing.exception.ForbiddenException
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
        val membership = serverMemberRepository.findByUserIdAndServerId(user.id!!, serverId)
            ?: throw BadRequestException("You are not a member of this server")

        // Only the server owner can create channels
        if (membership.role != MemberRole.OWNER) {
            throw ForbiddenException("Chỉ owner của server mới có quyền tạo channel")
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

    @Transactional
    fun renameChannel(
        serverId: String,
        channelId: String,
        request: UpdateChannelRequest,
        currentUserEmail: String
    ): ChannelResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        // Verify the server exists
        if (!serverRepository.existsById(serverId)) {
            throw ResourceNotFoundException("Server not found")
        }

        // Verify the user is a member and is the owner
        val membership = serverMemberRepository.findByUserIdAndServerId(user.id!!, serverId)
            ?: throw BadRequestException("You are not a member of this server")

        if (membership.role != MemberRole.OWNER) {
            throw ForbiddenException("Chỉ owner của server mới có quyền sửa tên channel")
        }

        // Find the channel and verify it belongs to this server
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }

        if (channel.server?.id != serverId) {
            throw BadRequestException("Channel does not belong to this server")
        }

        // Check name uniqueness (skip if name unchanged)
        if (channel.name != request.name &&
            channelRepository.existsByServerIdAndName(serverId, request.name)) {
            throw BadRequestException("A channel with the name '${request.name}' already exists in this server")
        }

        channel.name = request.name
        val updatedChannel = channelRepository.save(channel)

        return ChannelResponse.from(updatedChannel)
    }

    @Transactional
    fun deleteChannel(
        serverId: String,
        channelId: String,
        currentUserEmail: String
    ) {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw ResourceNotFoundException("User not found")

        // Verify the server exists
        if (!serverRepository.existsById(serverId)) {
            throw ResourceNotFoundException("Server not found")
        }

        // Verify the user is a member and is the owner
        val membership = serverMemberRepository.findByUserIdAndServerId(user.id!!, serverId)
            ?: throw BadRequestException("You are not a member of this server")

        if (membership.role != MemberRole.OWNER) {
            throw ForbiddenException("Chỉ owner của server mới có quyền xóa channel")
        }

        // Find the channel and verify it belongs to this server
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }

        if (channel.server?.id != serverId) {
            throw BadRequestException("Channel does not belong to this server")
        }

        channelRepository.delete(channel)
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
