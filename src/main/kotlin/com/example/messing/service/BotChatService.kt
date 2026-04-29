package com.example.messing.service

import com.example.messing.config.BotProperties
import com.example.messing.dto.bot.BotChatRequest
import com.example.messing.dto.bot.BotChatResult
import com.example.messing.dto.message.ChatMessageResponse
import com.example.messing.entity.ChannelType
import com.example.messing.entity.Message
import com.example.messing.entity.MessageType
import com.example.messing.entity.User
import com.example.messing.exception.ResourceNotFoundException
import com.example.messing.repository.ChannelRepository
import com.example.messing.repository.MessageRepository
import com.example.messing.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class BotChatService(
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val botProperties: BotProperties
) {

    private val logger = LoggerFactory.getLogger(BotChatService::class.java)
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(botProperties.timeoutMs))
        .build()

    @Transactional
    fun generateAnswer(request: BotChatRequest, principalIdentifier: String): BotChatResult {
        val channelId = request.channelId
        val question = request.question.trim()

        val channel = channelRepository.findById(channelId).orElseThrow {
            ResourceNotFoundException("Channel not found")
        }

        if (channel.type != ChannelType.TEXT) {
            throw IllegalArgumentException("Bot chỉ hoạt động trong text channel")
        }

        val sender = userRepository.findByEmailOrUsername(principalIdentifier, principalIdentifier)
            ?: throw ResourceNotFoundException("Sender not found for authenticated principal: $principalIdentifier")
        val server = channel.server ?: throw ResourceNotFoundException("Server not found")
        val botSender = getOrCreateBotUser(server)

        val userMessage = messageRepository.save(
            Message(
                content = question,
                type = MessageType.TEXT,
                sender = sender,
                channel = channel
            )
        )

        publishAfterCommit(channelId, toResponse(userMessage, channelId))

        val recentMessages = messageRepository.findTop10ByChannelIdOrderByCreatedAtDesc(channelId).reversed()
        val context = recentMessages
            .take(botProperties.recentMessageLimit)
            .joinToString(separator = "\n") { message ->
                val senderName = message.sender?.displayName ?: message.sender?.username ?: "Unknown"
                val content = if (message.type == MessageType.IMAGE) {
                    "[image] ${message.content}"
                } else {
                    message.content
                }
                "[$senderName]: $content"
            }

        logger.debug(
            "Built bot context channelId={} contextLength={}",
            channelId,
            context.length
        )

        val requestBody = """
            {
              "model": "${botProperties.model}",
              "messages": [
                {
                  "role": "system",
                  "content": ${toJsonString(botProperties.systemPrompt)}
                },
                {
                  "role": "user",
                  "content": ${toJsonString("Context gần nhất:\n$context\n\nCâu hỏi:\n$question")}
                }
              ],
              "stream": false
            }
        """.trimIndent()

        logger.debug(
            "AI request body summary model={} channelId={} body={}",
            botProperties.model,
            channelId,
            summarizeRequestBody(requestBody)
        )

        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("${botProperties.baseUrl.trimEnd('/')}/chat/completions"))
            .timeout(Duration.ofMillis(botProperties.timeoutMs))
            .header("Authorization", "Bearer ${botProperties.apiKey}")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)

        if (botProperties.httpReferer.isNotBlank()) {
            requestBuilder.header("HTTP-Referer", botProperties.httpReferer)
        }
        if (botProperties.xTitle.isNotBlank()) {
            requestBuilder.header("X-Title", botProperties.xTitle)
        }

        val requestHttp = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        logger.info(
            "Calling AI provider baseUrl={} model={} channelId={} questionLength={}",
            botProperties.baseUrl,
            botProperties.model,
            channelId,
            question.length
        )

        val content = try {
            val response = httpClient.send(requestHttp, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                logger.error(
                    "AI provider failed baseUrl={} model={} statusCode={} responseBody={}",
                    botProperties.baseUrl,
                    botProperties.model,
                    response.statusCode(),
                    response.body()
                )
                fallbackAnswer(question, context)
            } else {
                extractContent(response.body())
            }
        } catch (ex: Exception) {
            logger.warn(
                "AI provider unavailable baseUrl={} model={} reason={}",
                botProperties.baseUrl,
                botProperties.model,
                ex.message
            )
            fallbackAnswer(question, context)
        }

        val botMessage = messageRepository.save(
            Message(
                content = content,
                type = MessageType.TEXT,
                sender = botSender,
                channel = channel
            )
        )

        val botResponse = toResponse(botMessage, channelId)
        publishAfterCommit(channelId, botResponse)

        return BotChatResult(userMessage = toResponse(userMessage, channelId), botMessage = botResponse)
    }

    private fun toResponse(message: Message, channelId: String): ChatMessageResponse {
        val sender = message.sender ?: throw IllegalStateException("Sender is null")
        return ChatMessageResponse(
            id = message.id ?: throw IllegalStateException("Message id is null"),
            channelId = channelId,
            content = message.content,
            type = message.type,
            createdAt = message.createdAt,
            senderId = sender.id ?: throw IllegalStateException("Sender id is null"),
            senderUsername = sender.username,
            senderDisplayName = sender.displayName,
            senderAvatarUrl = sender.avatarUrl,
            metadata = null
        )
    }

    private fun toJsonString(value: String): String {
        return buildString {
            append('"')
            value.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
            append('"')
        }
    }

    private fun summarizeRequestBody(requestBody: String): String {
        val compact = requestBody
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return if (compact.length <= 600) compact else compact.take(600) + "..."
    }

    private fun publishAfterCommit(channelId: String, response: ChatMessageResponse) {
        val destination = "/topic/channels/$channelId"
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    messagingTemplate.convertAndSend(destination, response)
                }
            })
            return
        }

        messagingTemplate.convertAndSend(destination, response)
    }

    private fun getOrCreateBotUser(server: com.example.messing.entity.Server): User {
        val botUsername = "bot:server:${server.id}"
        val botEmail = botUsername
        val existingById: User? = server.botUserId?.let { botId ->
            val found = userRepository.findById(botId)
            if (found.isPresent) found.get() else null
        }
        val existingByUsername: User? = existingById ?: userRepository.findByUsername(botUsername)
        val existingByEmail: User? = existingByUsername ?: userRepository.  findByEmail(botEmail)

        val botUser: User = if (existingByEmail != null) {
            existingByEmail
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

        var changed = false
        if (botUser.username.isBlank()) {
            botUser.username = botUsername
            changed = true
        }
        if (botUser.displayName.isNullOrBlank()) {
            botUser.displayName = "Messing Bot"
            changed = true
        }
        if (botUser.email != botEmail) {
            botUser.email = botEmail
            changed = true
        }
        if (!botUser.isVirtual) {
            botUser.isVirtual = true
            changed = true
        }

        val saved = if (changed || botUser.id == null) userRepository.save(botUser) else botUser
        if (server.botUserId != saved.id) {
            server.botUserId = saved.id
        }
        return saved
    }

    private fun fallbackAnswer(question: String, context: String): String {
        return when {
            question.contains("tóm tắt", ignoreCase = true) || question.contains("summary", ignoreCase = true) -> {
                val lines = context.lines().filter { it.isNotBlank() }.takeLast(6)
                if (lines.isEmpty()) {
                    "Mình chưa thấy đủ dữ liệu để tóm tắt channel này."
                } else {
                    "Tóm tắt nhanh: " + lines.joinToString(" | ") { line ->
                        line.replace(Regex("^\\[[^]]+\\]:\\s*"), "")
                            .take(80)
                    }
                }
            }
            else -> "Mình chưa gọi được model AI để trả lời câu hỏi này. Bạn có thể thử lại sau hoặc hỏi một câu khác nhé!"
        }
    }

    private fun extractContent(body: String): String {
        val marker = "\"content\""
        val index = body.indexOf(marker)
        if (index == -1) return "Xin lỗi, mình chưa tạo được phản hồi."
        val colonIndex = body.indexOf(':', index + marker.length)
        if (colonIndex == -1) return "Xin lỗi, mình chưa tạo được phản hồi."
        var i = colonIndex + 1
        while (i < body.length && body[i].isWhitespace()) i++
        if (i >= body.length || body[i] != '"') return "Xin lỗi, mình chưa tạo được phản hồi."
        i++
        val output = StringBuilder()
        while (i < body.length) {
            val ch = body[i]
            if (ch == '"') break
            if (ch == '\\' && i + 1 < body.length) {
                val next = body[i + 1]
                when (next) {
                    '"' -> output.append('"')
                    '\\' -> output.append('\\')
                    '/' -> output.append('/')
                    'b' -> output.append('\b')
                    'f' -> output.append('\u000C')
                    'n' -> output.append('\n')
                    'r' -> output.append('\r')
                    't' -> output.append('\t')
                    'u' -> {
                        if (i + 5 < body.length) {
                            val hex = body.substring(i + 2, i + 6)
                            output.append(hex.toIntOrNull(16)?.toChar() ?: '?')
                            i += 4
                        }
                    }
                    else -> output.append(next)
                }
                i += 2
                continue
            }
            output.append(ch)
            i++
        }
        return output.toString().ifBlank { "Xin lỗi, mình chưa tạo được phản hồi." }
    }
}
