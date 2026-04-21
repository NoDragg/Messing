package com.example.messing.repository

import com.example.messing.entity.ServerInvite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface ServerInviteRepository : JpaRepository<ServerInvite, String> {
    fun existsByCode(code: String): Boolean
    fun findByCode(code: String): ServerInvite?

    @Modifying
    @Transactional
    @Query("delete from ServerInvite si where si.expiresAt < :expiresAt")
    fun deleteExpiredInvites(expiresAt: Instant): Int

    fun deleteAllByServerId(serverId: String)
}
