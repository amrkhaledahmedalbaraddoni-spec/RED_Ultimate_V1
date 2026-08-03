package com.red.server.services

import com.red.server.database.MessageDocument
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.stereotype.Service

@Service
class SearchService(private val mongoTemplate: MongoTemplate) {

    /**
     * بحث نصي شامل في أرشيف الرسائل المشفرة
     */
    fun searchInConversation(conversationId: String, keyword: String): List<MessageDocument> {
        val criteria = Criteria.where("conversationId").is(conversationId)
        val textCriteria = TextCriteria.forDefaultLanguage().matchingAny(keyword)
        
        val query = Query(criteria).addCriteria(textCriteria).limit(50)
        return mongoTemplate.find(query, MessageDocument::class.java)
    }
}
