package com.red.server.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

// Secondary entity - kept for legacy imports, delegates to primary
// Primary is com.red.server.database.MessageDocument

@Document(collection = "messages_legacy")
data class MessageDocumentLegacy(
    @Id val id: String,
    @Indexed val conversationId: String,
    @Indexed val senderId: String,
    @Indexed val receiverId: String,
    val payload: ByteArray,
    val type: String = "TEXT",
    val timestamp: Long = System.currentTimeMillis(),
    @Indexed val sequenceNumber: Long = 0,
    val isEdited: Boolean = false,
    val replyTo: String? = null
)

// Type alias for compatibility
typealias MessageDocument = com.red.server.database.MessageDocument
