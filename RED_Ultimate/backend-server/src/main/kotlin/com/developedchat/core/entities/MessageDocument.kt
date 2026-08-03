package com.red.core.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

@Document(collection = "messages")
class MessageDocument(
    @Id val uuid: String,
    @Indexed val senderId: String,
    @Indexed val receiverId: String,
    @Indexed val conversationId: String,
    val payload: ByteArray, // Encrypted content
    val type: String,
    val timestamp: Long,
    @Indexed val sequenceNumber: Long
)
