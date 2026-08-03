package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * RED Master Orchestration Service
 * The single source of truth for the Admin Panel.
 */
@Service
class MasterOrchestrationService(
    private val mongo: MongoTemplate,
    private val postgres: JdbcTemplate,
    private val redis: StringRedisTemplate
) {
    fun getFullSystemMetrics(): Map<String, Any> {
        return mapOf(
            "auth" to mapOf(
                "total_users" to postgres.queryForObject("SELECT count(*) FROM users", Int::class.java),
                "pending" to postgres.queryForObject("SELECT count(*) FROM users WHERE status = 'PENDING'", Int::class.java)
            ),
            "messaging" to mapOf(
                "messages_today" to mongo.count(org.springframework.data.mongodb.core.query.Query(), "messages"),
                "active_websockets" to redis.keys("red:presence:*").size
            ),
            "infrastructure" to mapOf(
                "db_status" to "HEALTHY",
                "redis_load" to "LOW"
            )
        )
    }
}
