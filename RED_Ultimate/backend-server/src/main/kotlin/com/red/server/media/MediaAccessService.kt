package com.red.server.media

import com.red.server.stories.StoryDocument
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

/** Object-level authorization for authenticated media downloads. */
@Service
class MediaAccessService(private val mongo: MongoTemplate) {
    fun requireDownloadAllowed(accountId: UUID, key: String) {
        val ownerId = key.substringAfter("users/", "").substringBefore('/')
        if (ownerId == accountId.toString()) return

        val activeStory = mongo.exists(
            Query(Criteria.where("mediaKey").`is`(key)
                .and("expiresAt").gt(Instant.now())
                .and("deletedAt").`is`(null)),
            StoryDocument::class.java
        )
        if (!activeStory) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Media object is not accessible to this account")
    }
}
