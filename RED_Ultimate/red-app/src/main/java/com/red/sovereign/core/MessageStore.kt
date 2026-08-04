package com.red.sovereign.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.red.sovereign.proto.RedProtos

data class StoredMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val encryptedPayload: ByteArray,
    val type: String,
    val senderDeviceId: Int,
    val receiverDeviceId: Int,
    val ciphertextType: Int,
    val sequence: Long,
    val status: String,
    val createdAt: Long
)

/** Stores E2EE ciphertext only. Plaintext must never be passed to this class. */
class MessageStore(context: Context) : SQLiteOpenHelper(context, "red_messages.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE messages (
            id TEXT PRIMARY KEY, conversation_id TEXT NOT NULL, sender_id TEXT NOT NULL,
            receiver_id TEXT NOT NULL, encrypted_payload BLOB NOT NULL, message_type TEXT NOT NULL,
            sender_device_id INTEGER NOT NULL, receiver_device_id INTEGER NOT NULL, ciphertext_type INTEGER NOT NULL,
            sequence_number INTEGER NOT NULL, status TEXT NOT NULL, created_at INTEGER NOT NULL,
            UNIQUE(conversation_id, sequence_number))""")
        db.execSQL("CREATE INDEX idx_messages_conversation ON messages(conversation_id, sequence_number DESC)")
        db.execSQL("CREATE INDEX idx_messages_status ON messages(status)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE messages ADD COLUMN sender_device_id INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiver_device_id INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE messages ADD COLUMN ciphertext_type INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun save(message: RedProtos.ChatMessage, status: String = "DELIVERED") {
        require(message.payload.size() > 0) { "Ciphertext is empty" }
        writableDatabase.insertWithOnConflict("messages", null, ContentValues().apply {
            put("id", message.id); put("conversation_id", message.conversationId); put("sender_id", message.senderId)
            put("receiver_id", message.receiverId); put("encrypted_payload", message.payload.toByteArray())
            put("message_type", message.type); put("sender_device_id", message.senderDeviceId)
            put("receiver_device_id", message.receiverDeviceId); put("ciphertext_type", message.ciphertextType)
            put("sequence_number", message.sequenceNumber); put("status", status); put("created_at", message.timestamp)
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun updateStatus(messageId: String, status: String) {
        require(status in setOf("SENT", "DELIVERED", "READ"))
        writableDatabase.update("messages", ContentValues().apply { put("status", status) }, "id = ?", arrayOf(messageId))
    }

    fun delete(messageId: String) { writableDatabase.delete("messages", "id = ?", arrayOf(messageId)) }

    fun messages(conversationId: String, limit: Int = 100): List<StoredMessage> {
        val result = mutableListOf<StoredMessage>()
        readableDatabase.query("messages", null, "conversation_id = ?", arrayOf(conversationId), null, null,
            "sequence_number DESC", limit.coerceIn(1, 500).toString()).use { cursor ->
            while (cursor.moveToNext()) result += StoredMessage(
                cursor.getString(cursor.getColumnIndexOrThrow("id")), cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("sender_id")), cursor.getString(cursor.getColumnIndexOrThrow("receiver_id")),
                cursor.getBlob(cursor.getColumnIndexOrThrow("encrypted_payload")), cursor.getString(cursor.getColumnIndexOrThrow("message_type")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sender_device_id")), cursor.getInt(cursor.getColumnIndexOrThrow("receiver_device_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("ciphertext_type")), cursor.getLong(cursor.getColumnIndexOrThrow("sequence_number")), cursor.getString(cursor.getColumnIndexOrThrow("status")),
                cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
            )
        }
        return result
    }
}
