package com.red.server.database

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisManager(
    private val redis: StringRedisTemplate? = null
) {
    // In-memory fallback if Redis not available
    private val fallbackPresence = mutableMapOf<String, String>()
    private val fallbackSeq = mutableMapOf<String, Long>()

    fun incrementSequence(conversationId: String): Long {
        return try {
            redis?.opsForValue()?.increment("red:seq:$conversationId") ?: incrementFallback(conversationId)
        } catch (e: Exception) {
            incrementFallback(conversationId)
        }
    }

    private fun incrementFallback(conversationId: String): Long {
        val current = fallbackSeq[conversationId] ?: 0L
        val next = current + 1
        fallbackSeq[conversationId] = next
        return next
    }

    fun setPresence(userId: String, isOnline: Boolean) {
        try {
            val status = if (isOnline) "ONLINE" else "OFFLINE"
            redis?.opsForValue()?.set("red:presence:$userId", status, 5, TimeUnit.MINUTES)
            fallbackPresence[userId] = status
        } catch (e: Exception) {
            fallbackPresence[userId] = if (isOnline) "ONLINE" else "OFFLINE"
        }
    }

    fun setPresence(userId: String, status: String) {
        try {
            redis?.opsForValue()?.set("red:presence:$userId", status, 5, TimeUnit.MINUTES)
            fallbackPresence[userId] = status
        } catch (e: Exception) {
            fallbackPresence[userId] = status
        }
    }

    fun isOnline(userId: String): Boolean {
        return try {
            val v = redis?.opsForValue()?.get("red:presence:$userId") ?: fallbackPresence[userId]
            v == "ONLINE" || v == "online"
        } catch (e: Exception) {
            fallbackPresence[userId] == "ONLINE"
        }
    }

    fun setTyping(userId: String, conversationId: String) {
        try {
            redis?.convertAndSend("red:typing", "$conversationId:$userId:${System.currentTimeMillis()}")
            redis?.opsForValue()?.set("red:typing:$conversationId:$userId", "true", 5, TimeUnit.SECONDS)
        } catch (e: Exception) {}
    }

    fun getTypingUsers(conversationId: String): List<String> {
        return try {
            val keys = redis?.keys("red:typing:$conversationId:*") ?: emptySet()
            keys.map { it.substringAfterLast(":").substringBefore(":") }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun publish(channel: String, message: String) {
        try {
            redis?.convertAndSend(channel, message)
        } catch (e: Exception) {}
    }

    fun getActiveUsers(): Int {
        return try {
            redis?.keys("red:presence:*")?.size ?: fallbackPresence.count { it.value == "ONLINE" }
        } catch (e: Exception) {
            fallbackPresence.count { it.value == "ONLINE" }
        }
    }
}
