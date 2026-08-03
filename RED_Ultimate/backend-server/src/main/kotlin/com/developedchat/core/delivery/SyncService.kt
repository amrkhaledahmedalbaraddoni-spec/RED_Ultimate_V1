package com.red.core.delivery

import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import com.red.proto.ChatProtos

@Service
class SyncService(private val mongoTemplate: MongoTemplate) {

    /**
     * ميزة التفوق: المزامنة الذكية
     * تجلب الرسائل التي لم تصل للمستخدم بناءً على آخر رقم تسلسلي (Sequence ID)
     */
    fun getPendingMessages(userId: String, lastSequence: Long): List<ChatProtos.ChatMessage> {
        val query = Query().addCriteria(
            Criteria.where("receiver_id").is(userId)
                .and("sequence_number").gt(lastSequence)
        )
        return mongoTemplate.find(query, ChatProtos.ChatMessage::class.java)
    }

    fun archiveMessage(message: ChatProtos.ChatMessage) {
        // يتم حفظ الرسالة مشفرة تماماً، السيرفر لا يرى المحتوى
        mongoTemplate.save(message)
    }
}
