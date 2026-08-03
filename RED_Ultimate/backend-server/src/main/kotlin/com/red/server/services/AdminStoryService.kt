package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

@Service
class AdminStoryService(private val mongoTemplate: MongoTemplate) {

    /**
     * جلب إحصائيات القصص للمدير
     */
    fun getStoryStats(): Map<String, Any> {
        val totalActive = mongoTemplate.count(Query(), "stories")
        return mapOf(
            "active_count" to totalActive,
            "storage_usage" to "${totalActive * 2} MB", // التقديري
            "last_cleanup" to System.currentTimeMillis()
        )
    }

    /**
     * حذف يدوي لكافة القصص (أمر تطهير)
     */
    fun purgeAllStories() {
        mongoTemplate.remove(Query(), "stories")
        println("🔴 RED ADMIN: Manual Story Purge Executed.")
    }
}
