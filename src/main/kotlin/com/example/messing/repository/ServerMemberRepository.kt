package com.example.messing.repository

import com.example.messing.entity.ServerMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ServerMemberRepository : JpaRepository<ServerMember, String> {

    fun findAllByUserId(userId: String): List<ServerMember>

    fun findAllByServerId(serverId: String): List<ServerMember>

    fun findByUserIdAndServerId(userId: String, serverId: String): ServerMember?

    fun existsByUserIdAndServerId(userId: String, serverId: String): Boolean

    @Query("""
        select sm.user.id
        from ServerMember sm
        where sm.server.id = :serverId
    """)
    fun findUserIdsByServerId(@Param("serverId") serverId: String): List<String>
}
