package com.red.server.controllers

import com.red.server.services.MasterStatsService
import com.red.server.services.CoreService
import com.red.server.auth.RedApprovalService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory

@RestController
@RequestMapping("/api/admin")
class AdminMonitorController(
    private val mongoTemplate: MongoTemplate? = null,
    private val redisTemplate: StringRedisTemplate? = null,
    private val statsService: MasterStatsService,
    private val coreService: CoreService? = null,
    private val approvalService: RedApprovalService? = null
) {
    @GetMapping("/monitor/stats")
    fun getMonitorStats(): Map<String, Any> {
        val base = statsService.getLiveMetrics()
        val runtime = Runtime.getRuntime()
        val uptime = try { ManagementFactory.getRuntimeMXBean().uptime } catch (e: Exception) { 0L }

        return base + mapOf(
            "active_users" to (base["active_users"] ?: 0),
            "total_messages" to (base["total_messages"] ?: 0),
            "messages_24h" to (base["messages_24h"] ?: 0),
            "system_load" to (base["system_load"] ?: 0),
            "uptime_ms" to uptime,
            "cpu_cores" to runtime.availableProcessors(),
            "jvm_memory_mb" to (runtime.totalMemory() / 1024 / 1024),
            "jvm_free_mb" to (runtime.freeMemory() / 1024 / 1024),
            "jvm_max_mb" to (runtime.maxMemory() / 1024 / 1024),
            "pending_users" to (approvalService?.getPendingList()?.size ?: 0),
            "approved_users" to (approvalService?.getApprovedUsers()?.size ?: 0),
            "groups" to (coreService?.getAllGroups()?.size ?: 0),
            "stories" to (coreService?.getActiveStories()?.size ?: 0),
            "timestamp" to System.currentTimeMillis()
        )
    }

    @GetMapping("/monitor/health")
    fun health() = getMonitorStats()

    @GetMapping("/monitor/realtime")
    fun realtime() = getMonitorStats()
}
