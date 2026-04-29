package com.example.messing.service

import com.example.messing.dto.server.CreateInviteResponse
import com.example.messing.dto.server.CreateServerRequest
import com.example.messing.dto.server.InviteAcceptResponse
import com.example.messing.dto.server.InviteMemberRequest
import com.example.messing.dto.server.ServerBotResponse
import com.example.messing.dto.server.ServerResponse
import com.example.messing.dto.server.UpdateServerBotRequest
import com.example.messing.dto.server.UpdateServerRequest
import com.example.messing.entity.Channel
import com.example.messing.entity.ChannelType
import com.example.messing.entity.MemberRole
import com.example.messing.entity.Server
import com.example.messing.entity.ServerInvite
import com.example.messing.entity.ServerMember
import com.example.messing.entity.User
import com.example.messing.exception.BadRequestException
import com.example.messing.exception.ForbiddenException
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.ServerInviteRepository
import com.example.messing.repository.ServerMemberRepository
import com.example.messing.repository.ServerRepository
import com.example.messing.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.security.SecureRandom
import java.time.Instant

@Service
class ServerService(
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val serverInviteRepository: ServerInviteRepository,
    private val fileStorageService: FileStorageService
) {

    private val inviteAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private val secureRandom = SecureRandom()

    @Transactional
    fun createServer(request: CreateServerRequest, currentUserIdentifier: String): ServerResponse {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val server = Server(
            name = request.name,
            iconUrl = request.iconUrl,
            owner = user
        )
        val savedServer = serverRepository.save(server)

        val ownerMember = ServerMember(
            user = user,
            server = savedServer,
            role = MemberRole.OWNER
        )
        serverMemberRepository.save(ownerMember)

        val botUser = createServerBotUser(savedServer)

        val generalChannel = Channel(
            name = "general",
            type = ChannelType.TEXT,
            server = savedServer
        )
        channelRepository.save(generalChannel)

        return ServerResponse.from(savedServer, botUser)
    }

    @Transactional
    fun inviteMember(serverId: String, request: InviteMemberRequest, currentUserIdentifier: String) {
        val inviter = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("Inviter not found")

        val server = serverRepository.findById(serverId).orElseThrow {
            ResourceNotFoundException("Server not found")
        }

        val inviterMembership = serverMemberRepository.findByUserIdAndServerId(inviter.id!!, serverId)
            ?: throw BadRequestException("Bạn không phải thành viên của server này")

        if (inviterMembership.role != MemberRole.OWNER && inviterMembership.role != MemberRole.ADMIN) {
            throw BadRequestException("Bạn không có quyền mời thành viên")
        }

        val invitedUser = userRepository.findByEmail(request.email)
            ?: throw ResourceNotFoundException("User được mời không tồn tại")

        if (serverMemberRepository.existsByUserIdAndServerId(invitedUser.id!!, serverId)) {
            throw BadRequestException("User đã là thành viên của server")
        }

        val member = ServerMember(
            user = invitedUser,
            server = server,
            role = MemberRole.MEMBER
        )

        serverMemberRepository.save(member)
    }

    @Transactional
    fun createInviteLink(serverId: String, currentUserIdentifier: String): CreateInviteResponse {
        val inviter = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("Inviter not found")

        val server = serverRepository.findById(serverId).orElseThrow {
            ResourceNotFoundException("Server not found")
        }

        val inviterMembership = serverMemberRepository.findByUserIdAndServerId(inviter.id!!, serverId)
            ?: throw BadRequestException("Bạn không phải thành viên của server này")

        if (inviterMembership.role != MemberRole.OWNER && inviterMembership.role != MemberRole.ADMIN) {
            throw BadRequestException("Bạn không có quyền tạo lời mời")
        }

        val code = generateUniqueInviteCode()
        val invite = ServerInvite(
            code = code,
            server = server,
            createdBy = inviter,
            expiresAt = Instant.now().plusSeconds(60L * 60L * 24L * 7L)
        )

        serverInviteRepository.save(invite)

        return CreateInviteResponse(
            code = code,
            inviteLink = "/invite/$code"
        )
    }

    @Transactional
    fun joinServerByInviteCode(code: String, currentUserIdentifier: String): InviteAcceptResponse {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val invite = serverInviteRepository.findByCode(code)
            ?: throw ResourceNotFoundException("Mã mời không tồn tại")

        if (invite.expiresAt != null && Instant.now().isAfter(invite.expiresAt)) {
            throw BadRequestException("Mã mời đã hết hạn")
        }

        val server = invite.server ?: throw ResourceNotFoundException("Server not found")
        val serverId = server.id ?: throw ResourceNotFoundException("Server not found")

        if (serverMemberRepository.existsByUserIdAndServerId(user.id!!, serverId)) {
            return InviteAcceptResponse(
                serverId = serverId,
                serverName = server.name,
                message = "Bạn đã là thành viên của server này"
            )
        }

        val member = ServerMember(
            user = user,
            server = server,
            role = MemberRole.MEMBER
        )
        serverMemberRepository.save(member)

        return InviteAcceptResponse(
            serverId = serverId,
            serverName = server.name,
            message = "Tham gia server thành công"
        )
    }

    fun getServersForUser(currentUserIdentifier: String): List<ServerResponse> {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val memberships = serverMemberRepository.findAllByUserId(user.id!!)
        return memberships.map { membership ->
            val server = membership.server
                ?: throw ResourceNotFoundException("Server not found")
            ServerResponse.from(server, resolveServerBotUser(server))
        }
    }

    @Transactional
    fun updateServerAvatar(serverId: String, file: MultipartFile, currentUserIdentifier: String): String {
        val requester = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val membership = serverMemberRepository.findByUserIdAndServerId(requester.id!!, serverId)
            ?: throw BadRequestException("Bạn không phải thành viên của server này")

        if (membership.role != MemberRole.OWNER) {
            throw BadRequestException("Chỉ owner mới có quyền đổi ảnh đại diện server")
        }

        val server = serverRepository.findById(serverId).orElseThrow {
            ResourceNotFoundException("Server not found")
        }

        val iconUrl = fileStorageService.storeCircularAvatar(file, "servers")
        server.iconUrl = iconUrl
        serverRepository.save(server)

        return iconUrl
    }

    @Transactional
    fun updateServerBot(serverId: String, request: UpdateServerBotRequest, currentUserIdentifier: String): ServerBotResponse {
        val requester = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val server = serverRepository.findById(serverId).orElseThrow {
            ResourceNotFoundException("Server not found")
        }

        val membership = serverMemberRepository.findByUserIdAndServerId(requester.id!!, serverId)
            ?: throw BadRequestException("Bạn không phải thành viên của server này")

        if (membership.role != MemberRole.OWNER) {
            throw ForbiddenException("Chỉ owner mới có quyền chỉnh sửa bot của server")
        }

        val botUser = resolveServerBotUser(server)
        request.displayName?.trim()?.takeIf { it.isNotBlank() }?.let {
            botUser.displayName = it
        }

        request.avatar?.let { avatarFile ->
            botUser.avatarUrl = fileStorageService.storeCircularAvatar(avatarFile, "users")
        }

        botUser.isVirtual = true

        val savedBot = userRepository.save(botUser)
        server.botUserId = savedBot.id
        serverRepository.save(server)

        return ServerBotResponse(
            serverId = serverId,
            botUserId = savedBot.id!!,
            username = savedBot.username,
            displayName = savedBot.displayName?.takeIf { it.isNotBlank() } ?: savedBot.username,
            avatarUrl = savedBot.avatarUrl,
            isVirtual = savedBot.isVirtual
        )
    }

    @Transactional
    fun updateServer(serverId: String, request: UpdateServerRequest, currentUserIdentifier: String): ServerResponse {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val server = serverRepository.findById(serverId)
            .orElseThrow { ResourceNotFoundException("Server not found") }

        if (server.owner?.id != user.id) {
            throw ForbiddenException("Chỉ owner của server mới có quyền chỉnh sửa server")
        }

        server.name = request.name.trim()
        val updated = serverRepository.save(server)

        return ServerResponse.from(updated, resolveServerBotUser(updated))
    }

    @Transactional
    fun deleteServer(serverId: String, currentUserIdentifier: String) {
        val user = userRepository.findByEmailOrUsername(currentUserIdentifier, currentUserIdentifier)
            ?: throw ResourceNotFoundException("User not found")

        val server = serverRepository.findById(serverId)
            .orElseThrow { ResourceNotFoundException("Server not found") }

        if (server.owner?.id != user.id) {
            throw ForbiddenException("Chỉ owner của server mới có quyền xóa server")
        }

        // Delete all invites for this server
        serverInviteRepository.deleteAllByServerId(serverId)

        serverRepository.delete(server)
    }

    private fun createServerBotUser(server: Server): User {
        val botUsername = "bot:server:${server.id}"
        val botEmail = botUsername
        val existing = userRepository.findByUsername(botUsername) ?: userRepository.findByEmail(botEmail)

        val bot = if (existing != null) {
            existing
        } else {
            User(
                username = botUsername,
                displayName = "Messing Bot",
                email = botEmail,
                password = "__bot_account__",
                avatarUrl = null,
                isVirtual = true
            )
        }

        bot.username = botUsername
        bot.displayName = bot.displayName?.takeIf { it.isNotBlank() } ?: "Messing Bot"
        bot.email = botEmail
        bot.isVirtual = true

        val savedBot = userRepository.save(bot)
        server.botUserId = savedBot.id
        serverRepository.save(server)
        return savedBot
    }

    private fun resolveServerBotUser(server: Server): User {
        val botUserId = server.botUserId
        if (!botUserId.isNullOrBlank()) {
            userRepository.findById(botUserId).orElse(null)?.let { return it }
        }
        return createServerBotUser(server)
    }

    private fun generateUniqueInviteCode(length: Int = 6): String {
        var code: String
        do {
            code = buildString {
                repeat(length) {
                    append(inviteAlphabet[secureRandom.nextInt(inviteAlphabet.length)])
                }
            }
        } while (serverInviteRepository.existsByCode(code))

        return code
    }
}
