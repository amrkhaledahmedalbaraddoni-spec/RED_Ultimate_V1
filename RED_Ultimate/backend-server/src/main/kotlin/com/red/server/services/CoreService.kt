package com.red.server.services

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CoreService(private val mongoTemplate: MongoTemplate) {
    private val groups = ConcurrentHashMap<String, GroupEntity>()

    fun createGroup(group: GroupEntity) {
        groups[group.groupId] = group
    }

    fun getGroup(id: String): GroupEntity? = groups[id]

    fun saveStory(story: StoryEntity) {
        mongoTemplate.save(story)
    }

    fun getActiveStoriesCount(): Map<String, Long> {
        val now = System.currentTimeMillis()
        val count = mongoTemplate.count(
            Query(Criteria.where("expiresAt").gt(now)),
            StoryEntity::class.java
        )
        return mapOf("activeStories" to count)
    }

    fun getAggregatedStats(): Map<String, Any> = mapOf(
        "groups" to groups.size,
        "activeStories" to getActiveStoriesCount().getValue("activeStories"),
        "timestamp" to System.currentTimeMillis()
    )

    @Scheduled(fixedRate = 60_000)
    fun cleanupStories() {
        val query = Query(Criteria.where("expiresAt").lte(System.currentTimeMillis()))
        mongoTemplate.findAllAndRemove(query, StoryEntity::class.java)
    }
}

data class GroupEntity(
    val groupId: String,
    val name: String,
    val ownerId: String,
    val memberIds: Set<String> = emptySet()
)

@Document(collection = "stories")
data class StoryEntity(
    @Id val id: String,
    val ownerId: String,
    val encryptedMediaUrl: String,
    val createdAt: Long,
    val expiresAt: Long
)
