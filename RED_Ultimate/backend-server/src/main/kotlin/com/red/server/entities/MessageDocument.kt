package com.red.server.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

@Document(collection = "messages")
class MessageDocument(
    @Id val id: String, // UUID v7
    @Indexed val conversationId: String,
    @Indexed val senderId: String,
    @Indexed val receiverId: String,
    val payload: ByteArray,   // المحتوى المشفر
    val type: String,
    val timestamp: Long,
    @Indexed val sequenceNumber: Long, // الرقم التسلسلي العالمي
    val isEdited: Boolean = false,
    val replyTo: String? = null
)
