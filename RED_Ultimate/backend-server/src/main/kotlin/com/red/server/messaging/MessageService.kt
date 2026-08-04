package com.red.server.messaging

import com.red.sovereign.proto.RedProtos
import com.red.server.database.MessageDocument
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * RED Message Service — handles message persistence, deduplication, sequencing, and delivery.
 */
@Service
class MessageService(
    private val mongoTemplate: MongoTemplate,
    private val redisTemplate: RedisTemplate<String, String>
) {
    // Dedup cache: message UUID -> timestamp
    private val dedupCache = mutableMapOf<String, Long>()

    /**
     * Process incoming message: dedup → sequence → store → notify
     */
    fun processIncoming(message: RedProtos.ChatMessage): MessageDocument = processIncoming(
        messageId = message.id,
        senderId = message.senderId,
        receiverId = message.receiverId,
        conversationId = message.conversationId,
        payload = message.payload.toByteArray(),
        messageType = message.type.ifBlank { "TEXT" }
    )

    fun processIncoming(
        senderId: String,
        receiverId: String,
        conversationId: String,
        payload: ByteArray,
        messageType: String = "TEXT",
        messageId: String = UUID.randomUUID().toString()
    ): MessageDocument {
        val messageUuid = messageId.ifBlank { UUID.randomUUID().toString() }

        // 1. Dedup check
        if (dedupCache.containsKey(messageUuid)) {
            throw DuplicateMessageException("Message $messageUuid already processed")
        }
        dedupCache[messageUuid] = System.currentTimeMillis()
        
        // 2. Generate sequence number
        val sequence = redisTemplate.opsForValue()
            .increment("red:seq:$conversationId") ?: 1L

        // 3. Store message
        val message = MessageDocument(
            id = null,
            uuid = messageUuid,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            payload = payload,
            messageType = messageType,
            sequenceNumber = sequence,
            status = "DELIVERED",
            createdAt = Instant.now(),
            deliveredAt = null,
            readAt = null
        )
        mongoTemplate.save(message)

        // 4. Update presence
        redisTemplate.opsForValue().set(
            "red:presence:$senderId",
            "online",
            java.time.Duration.ofMinutes(5)
        )

        // 5. Notify via Redis pub/sub
        redisTemplate.convertAndSend(
            "red:messages:$receiverId",
            "$messageUuid|$conversationId|$sequence"
        )

        return message
    }

    /**
     * Get messages for a conversation with pagination
     */
    fun getMessages(conversationId: String, limit: Int = 50, beforeSequence: Long? = null): List<MessageDocument> {
        val query = Query(Criteria.where("conversationId").`is`(conversationId))
        if (beforeSequence != null) {
            query.addCriteria(Criteria.where("sequenceNumber").lt(beforeSequence))
        }
        query.limit(limit)
        return mongoTemplate.find(query, MessageDocument::class.java)
    }

    fun getMissedMessages(
        conversationId: String,
        fromSequence: Long,
        toSequence: Long,
        limit: Int = 500
    ): List<MessageDocument> {
        val safeLimit = limit.coerceIn(1, 500)
        val criteria = Criteria.where("conversationId").`is`(conversationId)
            .and("sequenceNumber").gte(fromSequence)
        if (toSequence > 0) criteria.lte(toSequence)
        return mongoTemplate.find(
            Query(criteria).with(Sort.by(Sort.Direction.ASC, "sequenceNumber")).limit(safeLimit),
            MessageDocument::class.java
        )
    }

    /**
     * Acknowledge message delivery
     */
    fun acknowledgeDelivery(messageUuid: String) {
        val query = Query(Criteria.where("uuid").`is`(messageUuid))
        val message = mongoTemplate.findOne(query, MessageDocument::class.java)
        message?.let {
            it.deliveredAt = Instant.now()
            it.status = "DELIVERED"
            mongoTemplate.save(it)
        }
    }

    /**
     * Mark message as read
     */
    fun markAsRead(messageUuid: String) {
        val query = Query(Criteria.where("uuid").`is`(messageUuid))
        val message = mongoTemplate.findOne(query, MessageDocument::class.java)
        message?.let {
            it.readAt = Instant.now()
            it.status = "READ"
            mongoTemplate.save(it)
        }
    }

    class DuplicateMessageException(message: String) : RuntimeException(message)
}
