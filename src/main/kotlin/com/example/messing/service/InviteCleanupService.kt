package com.example.messing.service

import com.example.messing.repository.ServerInviteRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InviteCleanupService(
    private val serverInviteRepository: ServerInviteRepository
) {

    private val log = LoggerFactory.getLogger(InviteCleanupService::class.qualifiedName)

    /**
     * Chạy mỗi 30 phút để dọn lời mời đã hết hạn.
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000L)
    @Transactional
    fun cleanupExpiredInvites() {
        val deletedCount = serverInviteRepository.deleteExpiredInvites(Instant.now())
        if (deletedCount > 0) {
            log.info("Invite cleanup: deleted {} expired invite(s)", deletedCount)
        }
    }
}
