package com.example.messing.service

import com.example.messing.entity.MemberRole
import com.example.messing.entity.ServerMember
import com.example.messing.exception.BadRequestException
import com.example.messing.exception.ForbiddenException
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ServerMemberRepository
import org.springframework.stereotype.Service

@Service
class ServerPermissionService(
    private val serverMemberRepository: ServerMemberRepository
) {
    fun requireMembership(userId: String, serverId: String): ServerMember {
        return serverMemberRepository.findByUserIdAndServerId(userId, serverId)
            ?: throw BadRequestException("Bạn không phải thành viên của server này")
    }

    fun requireOwner(membership: ServerMember, message: String) {
        if (membership.role != MemberRole.OWNER) {
            throw ForbiddenException(message)
        }
    }

    fun requireAdminOrOwner(membership: ServerMember, message: String) {
        if (membership.role != MemberRole.OWNER && membership.role != MemberRole.ADMIN) {
            throw BadRequestException(message)
        }
    }

    fun requireChannelOwnership(serverId: String, channelServerId: String) {
        if (serverId != channelServerId) {
            throw ResourceNotFoundException("Channel not found")
        }
    }
}
