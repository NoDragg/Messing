package com.example.messing.repository

import com.example.messing.entity.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, String> {

    // Timeline theo ascending order cho realtime/render history đơn giản hơn.
    fun findAllByChannelIdOrderByCreatedAtAsc(channelId: String): List<Message>

    // Page-based load cho lịch sử chat khi cần phân trang sâu.
    fun findAllByChannelId(channelId: String, pageable: Pageable): Page<Message>

    fun findByChannelIdOrderByCreatedAtDesc(channelId: String, pageable: Pageable): Page<Message>

    fun findTop10ByChannelIdOrderByCreatedAtDesc(channelId: String): List<Message>
}
