package com.red.server.services

import com.red.server.stories.StoryService
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CoreService(private val stories: StoryService) {
    private val groups = ConcurrentHashMap<String, GroupEntity>()
    fun createGroup(group: GroupEntity) { groups[group.groupId] = group }
    fun getGroup(id: String): GroupEntity? = groups[id]
    fun getActiveStoriesCount(): Map<String, Long> = mapOf("activeStories" to stories.activeCount())
    fun getAggregatedStats(): Map<String, Any> = mapOf(
        "groups" to groups.size,
        "activeStories" to stories.activeCount(),
        "timestamp" to System.currentTimeMillis()
    )
}

data class GroupEntity(val groupId: String, val name: String, val ownerId: String, val memberIds: Set<String> = emptySet())
