package com.example.messing.repository

import com.example.messing.entity.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, String> {

    fun findAllByChannelIdOrderByCreatedAtAsc(channelId: String): List<Message>

    fun findAllByChannelId(channelId: String, pageable: Pageable): Page<Message>

    fun findByChannelIdOrderByCreatedAtDesc(channelId: String, pageable: Pageable): Page<Message>
}
