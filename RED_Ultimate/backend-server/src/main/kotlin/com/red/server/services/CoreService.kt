package com.red.server.services

import com.red.sovereign.features.chat.GroupEntity
import com.red.sovereign.features.stories.StoryEntity
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import java.util.concurrent.ConcurrentHashMap

@Service
class CoreService(private val mongoTemplate: MongoTemplate) {

    private val groups = ConcurrentHashMap<String, GroupEntity>()

    // 1. نظام المجموعات
    fun createGroup(group: GroupEntity) {
        groups[group.groupId] = group
        println("🔴 RED: Group ${group.name} created by ${group.ownerId}")
    }

    fun getGroup(id: String) = groups[id]

    // 2. نظام القصص (Auto-Delete Cron)
    fun saveStory(story: StoryEntity) {
        mongoTemplate.save(story)
    }

    @Scheduled(fixedRate = 60000) // كل دقيقة
    fun cleanupStories() {
        val now = System.currentTimeMillis()
        val query = Query(Criteria.where("expiresAt").lte(now))
        val expired = mongoTemplate.findAllAndRemove(query, StoryEntity::class.java)
        if (expired.isNotEmpty()) {
            println("🧹 RED Cleanup: Deleted ${expired.size} expired stories.")
            // Logic to delete from MinIO would go here
        }
    }
}
