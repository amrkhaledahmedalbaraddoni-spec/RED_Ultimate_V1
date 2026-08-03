package com.red.server.messaging

import com.red.sovereign.proto.ChatProtos
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class DeleteService(private val mongoTemplate: MongoTemplate) {

    fun deleteForEveryone(messageId: String, senderId: String): Boolean {
        val query = Query(Criteria.where("id").is(messageId).and("senderId").is(senderId))
        val deleted = mongoTemplate.remove(query, "messages")
        return deleted.deletedCount > 0
        // السيرفر سيقوم ببث إشارة DELETE عبر WebSocket لبقية الأطراف
    }
}
