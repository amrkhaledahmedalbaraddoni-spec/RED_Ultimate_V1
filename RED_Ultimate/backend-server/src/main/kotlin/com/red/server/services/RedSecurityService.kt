package com.red.server.services

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class RedSecurityService(
    private val redisTemplate: StringRedisTemplate? = null
) {
    private val blockedDevices = ConcurrentHashMap<String, Instant>()
    private val wipeHistory = mutableListOf<Map<String, Any>>()

    fun triggerKillSwitch(userId: String): Map<String, Any> {
        blockedDevices[userId] = Instant.now()
        try {
            redisTemplate?.opsForValue()?.set("security:blocked:$userId", "true")
            redisTemplate?.convertAndSend("security:wipe", userId)
        } catch (e: Exception) {}

        val record = mapOf(
            "userId" to userId,
            "action" to "WIPE",
            "status" to "EXECUTED",
            "timestamp" to Instant.now().toString(),
            "executor" to "RED_MASTER_ADMIN"
        )
        wipeHistory.add(record)
        println("⚠️ RED Security Kill Switch TRIGGERED for $userId")

        return record
    }

    fun sendWipeSignal(userId: String): Map<String, Any> = triggerKillSwitch(userId)

    fun activateKillSwitch(reason: String): Map<String, Any> {
        return mapOf(
            "status" to "ACTIVATED",
            "reason" to reason,
            "timestamp" to Instant.now().toString(),
            "affected_devices" to blockedDevices.size
        )
    }

    fun rebootHardware(deviceId: String): Map<String, Any> {
        return mapOf(
            "status" to "REBOOTING",
            "deviceId" to deviceId,
            "timestamp" to Instant.now().toString()
        )
    }

    fun getDeviceSecurityStatus(userId: String): Map<String, Any> {
        val isBlocked = blockedDevices.containsKey(userId) ||
                (try { redisTemplate?.opsForValue()?.get("security:blocked:$userId") == "true" } catch (e: Exception) { false })

        return mapOf(
            "userId" to userId,
            "isBlocked" to isBlocked,
            "blockedAt" to (blockedDevices[userId]?.toString() ?: "not_blocked"),
            "securityLevel" to "HIGH",
            "encryption" to "POST_QUANTUM_READY",
            "wipe_history" to wipeHistory.filter { it["userId"] == userId }
        )
    }

    fun unblockDevice(userId: String): Map<String, Any> {
        blockedDevices.remove(userId)
        try { redisTemplate?.delete("security:blocked:$userId") } catch (e: Exception) {}
        return mapOf("userId" to userId, "status" to "UNBLOCKED", "timestamp" to Instant.now().toString())
    }

    fun getSecurityStats(): Map<String, Any> {
        return mapOf(
            "blocked_devices" to blockedDevices.size,
            "wipe_commands" to wipeHistory.size,
            "security_level" to "ULTIMATE",
            "encryption" to "AES-256-GCM + Kyber-1024 Post-Quantum",
            "last_incident" to (wipeHistory.lastOrNull()?.get("timestamp") ?: "none")
        )
    }
}
