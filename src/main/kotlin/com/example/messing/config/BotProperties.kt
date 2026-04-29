package com.example.messing.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.bot")
data class BotProperties(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val timeoutMs: Long = 60000,
    val recentMessageLimit: Int = 10,
    val systemPrompt: String = "Bạn là Bot hỗ trợ chat trong channel. Trả lời ngắn gọn, rõ ràng, đúng trọng tâm. Nếu không đủ dữ liệu, hãy nói rõ là chưa đủ thông tin. Chỉ trả lời bằng tiếng Việt.",
    val httpReferer: String = "",
    val xTitle: String = ""
)
