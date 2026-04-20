package com.example.messing.repository

import com.example.messing.entity.Channel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepository : JpaRepository<Channel, String> {

    // Channels được load theo server để build channel sidebar và message context.
    fun findAllByServerId(serverId: String): List<Channel>

    // Unique check ở tầng DB logic để tránh trùng tên trong cùng một server.
    fun existsByServerIdAndName(serverId: String, name: String): Boolean
}
