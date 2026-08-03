package com.red.server.messaging

import com.red.server.database.MessageDocument
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class AdvancedMessageService(
    private val mongoTemplate: MongoTemplate? = null
) {
    fun processDeleteRequest(messageId: String, senderId: String): List<String> {
        return try {
            val query = Query(Criteria.where("uuid").`is`(messageId).and("senderId").`is`(senderId))
            val message = mongoTemplate?.findOne(query, MessageDocument::class.java)
            if (message != null) {
                mongoTemplate?.remove(query, MessageDocument::class.java)
                println("🔴 RED: Message $messageId deleted for everyone by $senderId")
                listOf(message.receiverId)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun editMessage(messageId: String, senderId: String, newContent: ByteArray) {
        try {
            val query = Query(Criteria.where("uuid").`is`(messageId).and("senderId").`is`(senderId))
            val update = Update().set("payload", newContent).set("isEdited", true)
            mongoTemplate?.updateFirst(query, update, MessageDocument::class.java)
            println("🔴 RED: Message $messageId edited by $senderId")
        } catch (e: Exception) {}
    }

    fun deleteForEveryone(messageId: String, conversationId: String): Boolean {
        return processDeleteRequest(messageId, "system").isNotEmpty() || run {
            try {
                val q = Query(Criteria.where("uuid").`is`(messageId).and("conversationId").`is`(conversationId))
                mongoTemplate?.remove(q, MessageDocument::class.java)
                true
            } catch (e: Exception) { false }
        }
    }
}
