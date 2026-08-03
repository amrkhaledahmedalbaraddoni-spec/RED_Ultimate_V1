package com.red.server.database

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "messages")
data class MessageDocument(
    @Id val id: String? = null,
    val uuid: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val payload: ByteArray,
    val messageType: String = "TEXT",
    val sequenceNumber: Long = 0,
    val status: String = "SENT",
    val createdAt: Instant = Instant.now(),
    var deliveredAt: Instant? = null,
    var readAt: Instant? = null
)
