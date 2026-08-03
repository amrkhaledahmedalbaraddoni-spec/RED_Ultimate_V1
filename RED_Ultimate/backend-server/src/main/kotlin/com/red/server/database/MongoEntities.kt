package com.red.server.database

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

/**
 * مخزن الرسائل الضخم - مصحح لدعم البحث والمزامنة
 */
@Document(collection = "messages")
data class MessageDocument(
    @Id val id: String, // UUID v7
    @Indexed val conversationId: String,
    @Indexed val senderId: String,
    val type: String,
    val payload: ByteArray, // مشفر تماماً
    val timestamp: Long,
    @Indexed val sequenceNumber: Long,
    val isEdited: Boolean = false,
    val replyTo: String? = null
)
