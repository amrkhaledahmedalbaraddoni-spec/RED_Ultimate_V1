package com.red.core.delivery

import com.red.proto.ChatProtos
import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.atomic.AtomicLong

@Service
class MessageService(
    private val mongoTemplate: MongoTemplate,
    private val redisTemplate: StringRedisTemplate
) {
    /**
     * Algorithm: Receive -> Dedup -> Sequence -> Store -> ACK
     */
    fun processIncomingMessage(msg: ChatProtos.ChatMessage): Long {
        // 1. Check Redis for Dedup (UUID v7)
        val cacheKey = "msg_dedup:${msg.id}"
        if (redisTemplate.hasKey(cacheKey)) return -1 // Duplicate

        // 2. Generate Sequence Number for this conversation
        val seqKey = "conv_seq:${msg.conversation_id}"
        val sequenceNumber = redisTemplate.opsForValue().increment(seqKey) ?: 0L

        // 3. Save to MongoDB
        mongoTemplate.save(msg)
        
        // 4. Mark as processed in Redis (expires in 24h)
        redisTemplate.opsForValue().set(cacheKey, "1", 24, java.util.concurrent.TimeUnit.HOURS)

        return sequenceNumber
    }

    fun getMissedMessages(conversationId: String, lastSeq: Long): List<ChatProtos.ChatMessage> {
        val query = Query(Criteria.where("conversation_id").is(conversationId)
            .and("sequence_number").gt(lastSeq))
        return mongoTemplate.find(query, ChatProtos.ChatMessage::class.java)
    }
}
