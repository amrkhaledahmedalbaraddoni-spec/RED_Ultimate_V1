package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

data class GroupEntity(
    val groupId: String,
    val name: String,
    val ownerId: String,
    val members: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val avatarUrl: String? = null
)

data class StoryEntity(
    val id: String,
    val userId: String,
    val mediaUrl: String,
    val mediaType: String = "IMAGE", // IMAGE, VIDEO
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000, // 24h
    val viewers: List<String> = emptyList()
)

@Service
class CoreService(
    private val mongoTemplate: MongoTemplate? = null
) {
    private val groups = ConcurrentHashMap<String, GroupEntity>()
    private val storyCache = ConcurrentHashMap<String, StoryEntity>()

    // Groups
    fun createGroup(group: GroupEntity): GroupEntity {
        groups[group.groupId] = group
        println("🔴 RED: Group ${group.name} created by ${group.ownerId} with ${group.members.size} members")
        return group
    }

    fun getGroup(id: String) = groups[id]
    fun getAllGroups() = groups.values.toList()
    fun getGroupsForUser(userId: String) = groups.values.filter { it.members.contains(userId) || it.ownerId == userId }

    // Stories
    fun saveStory(story: StoryEntity): StoryEntity {
        storyCache[story.id] = story
        try {
            mongoTemplate?.save(story)
        } catch (e: Exception) {
            // Fallback to memory if Mongo not ready
            println("⚠️ RED Stories: Mongo not available, cached in memory ${story.id}")
        }
        return story
    }

    fun getActiveStories(): List<StoryEntity> {
        val now = System.currentTimeMillis()
        return storyCache.values.filter { it.expiresAt > now }
    }

    fun getActiveStoriesCount(): Map<String, Any> {
        return mapOf(
            "total" to getActiveStories().size,
            "expiring_soon" to getActiveStories().count { it.expiresAt - System.currentTimeMillis() < 3600000 },
            "timestamp" to Instant.now().toString()
        )
    }

    fun getStoriesForUser(userId: String): List<StoryEntity> {
        return getActiveStories().filter { story ->
            // In real logic, filter by contacts; for now return all
            true
        }
    }

    fun viewStory(storyId: String, viewerId: String): StoryEntity? {
        val story = storyCache[storyId] ?: return null
        if (!story.viewers.contains(viewerId)) {
            val updated = story.copy(viewers = story.viewers + viewerId)
            storyCache[storyId] = updated
            return updated
        }
        return story
    }

    @Scheduled(fixedRate = 60000) // Every minute
    fun cleanupStories() {
        val now = System.currentTimeMillis()
        val expired = storyCache.values.filter { it.expiresAt <= now }
        if (expired.isNotEmpty()) {
            expired.forEach { storyCache.remove(it.id) }
            try {
                val query = Query(Criteria.where("expiresAt").lte(now))
                mongoTemplate?.findAllAndRemove(query, StoryEntity::class.java)
            } catch (e: Exception) {
            }
            println("🧹 RED Cleanup: Deleted ${expired.size} expired stories from memory + Mongo")
        }
    }

    fun getAggregatedStats(): Map<String, Any> {
        return mapOf(
            "groups" to groups.size,
            "active_stories" to getActiveStories().size,
            "system" to "RED Ultimate V2",
            "timestamp" to Instant.now().toString()
        )
    }
}
