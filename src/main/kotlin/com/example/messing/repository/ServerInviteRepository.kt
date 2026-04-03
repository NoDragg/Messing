package com.example.messing.repository

import com.example.messing.entity.ServerInvite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ServerInviteRepository : JpaRepository<ServerInvite, String> {
    fun existsByCode(code: String): Boolean
    fun findByCode(code: String): ServerInvite?
    fun deleteByExpiresAtBefore(expiresAt: Instant): Long
}
