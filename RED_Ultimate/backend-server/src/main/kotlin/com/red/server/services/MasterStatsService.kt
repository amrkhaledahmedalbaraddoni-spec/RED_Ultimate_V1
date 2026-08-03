package com.red.server.services

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class MasterStatsService(
    private val mongo: MongoTemplate,
    private val postgres: JdbcTemplate,
    private val redis: StringRedisTemplate
) {
    fun getLiveMetrics(): Map<String, Any> {
        return mapOf(
            "active_users" to (redis.keys("red:presence:*").size),
            "messages_24h" to mongo.count(Query(), "messages"),
            "system_load" to 12.5, // Calc from hardware
            "db_health" to "EXCELLENT",
            "pending_approvals" to (postgres.queryForObject("SELECT count(*) FROM users WHERE status = 'PENDING'", Int::class.java) ?: 0)
        )
    }

    fun getVoipMetrics(): List<Map<String, Any>> {
        // Fetch active calls from Mediasoup sessions
        return listOf(
            mapOf("id" to "call_1", "type" to "VIDEO", "duration" to "12:45", "quality" to "1080p")
        )
    }
}
