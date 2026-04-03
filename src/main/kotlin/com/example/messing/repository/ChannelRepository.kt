package com.example.messing.repository

import com.example.messing.entity.Channel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepository : JpaRepository<Channel, String> {

    fun findAllByServerId(serverId: String): List<Channel>

    fun existsByServerIdAndName(serverId: String, name: String): Boolean
}
