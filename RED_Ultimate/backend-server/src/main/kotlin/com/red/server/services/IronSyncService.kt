package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

@Service
class IronSyncService(private val redis: StringRedisTemplate) {

    /**
     * تتبع آخر حالة للجهاز (Device State Vector)
     */
    fun updateStateVector(userId: String, deviceId: String, lastSeq: Long) {
        val key = "red:vector:$userId:$deviceId"
        redis.opsForValue().set(key, lastSeq.toString(), 30, TimeUnit.DAYS)
    }

    /**
     * جلب الفروقات بين الأجهزة (Gap Delta)
     */
    fun getDeviceGaps(userId: String): Map<String, Long> {
        val keys = redis.keys("red:vector:$userId:*")
        return keys.associate { it.split(":").last() to (redis.opsForValue().get(it)?.toLong() ?: 0L) }
    }
}
