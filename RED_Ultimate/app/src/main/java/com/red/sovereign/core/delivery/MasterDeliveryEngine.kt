package com.red.sovereign.core.delivery

import com.red.sovereign.core.database.RedDao
import com.red.sovereign.core.network.RedWebSocketClient
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*

@Singleton
class MasterDeliveryEngine @Inject constructor(
    private val redDao: RedDao,
    private val webSocketClient: RedWebSocketClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun generateUuidV7(): String {
        val timestamp = System.currentTimeMillis()
        val random = SecureRandom()
        val msb = (timestamp shl 16) or 0x7000L or (random.nextLong() and 0x0FFFL)
        val lsb = (random.nextLong() and 0x3FFFFFFFFFFFFFFFL) or Long.MIN_VALUE
        return UUID(msb, lsb).toString()
    }

    fun initialize() {
        println("🔴 RED: Master Delivery Engine Initialized with UUID v7 - System C ACTIVE")
    }

    // Primary API used by ChatViewModel
    fun dispatchMessage(conversationId: String, text: String) {
        scope.launch {
            try {
                // Delegate to RedDeliveryEngine if available, else simple send
                val msgId = generateUuidV7()
                val payload = """{"id":"$msgId","conversationId":"$conversationId","content":"$text","type":"TEXT","ts":${System.currentTimeMillis()}}"""
                webSocketClient.send(payload.toByteArray())
                println("🔴 RED Delivery: dispatched $msgId to $conversationId")
            } catch (e: Exception) {
                println("❌ RED Delivery failed: ${e.message}")
            }
        }
    }

    fun dispatchMedia(conversationId: String, filePath: String, mimeType: String) {
        dispatchMessage(conversationId, "[MEDIA:$mimeType]$filePath")
    }

    fun syncMissingMessages(conversationId: String, fromSeq: Long, toSeq: Long) {
        scope.launch {
            val syncReq = """{"type":"sync","conversationId":"$conversationId","from":$fromSeq,"to":$toSeq}"""
            webSocketClient.send(syncReq.toByteArray())
        }
    }

    fun markDelivered(messageId: String) {
        scope.launch {
            val ack = """{"type":"ack","messageId":"$messageId","status":"DELIVERED"}"""
            webSocketClient.send(ack.toByteArray())
        }
    }

    fun markRead(messageId: String) {
        scope.launch {
            val ack = """{"type":"ack","messageId":"$messageId","status":"READ"}"""
            webSocketClient.send(ack.toByteArray())
        }
    }
}
