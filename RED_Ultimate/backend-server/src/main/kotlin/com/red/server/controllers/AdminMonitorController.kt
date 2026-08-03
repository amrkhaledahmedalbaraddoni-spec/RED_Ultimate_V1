package com.red.server.controllers

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminMonitorController(
    private val mongoTemplate: MongoTemplate,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @GetMapping("/monitor/stats")
    fun getMonitorStats(): Map<String, Any> {
        val activeUsers = redisTemplate.keys("red:presence:*").size
        val totalMessages = mongoTemplate.getCollection("messages").countDocuments()
        
        return mapOf(
            "active_users" to activeUsers,
            "total_messages" to totalMessages,
            "system_load" to (Runtime.getRuntime().let { 
                ((it.totalMemory - it.freeMemory).toDouble() / it.maxMemory * 100).toInt() 
            }),
            "uptime_ms" to ManagementFactory_getUptime(),
            "cpu_cores" to Runtime.getRuntime().availableProcessors(),
            "jvm_memory_mb" to (Runtime.getRuntime().totalMemory() / 1024 / 1024),
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    private fun ManagementFactory_getUptime(): Long {
        return try {
            java.lang.management.ManagementFactory.getRuntimeMXBean().uptime
        } catch (e: Exception) { 0L }
    }
}
