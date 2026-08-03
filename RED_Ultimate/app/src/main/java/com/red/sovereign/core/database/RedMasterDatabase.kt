package com.red.sovereign.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages", indices = [Index("uuid", unique = true), Index("conversationId"), Index("sequenceNumber")])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String,
    val conversationId: String,
    val senderId: String,
    val type: String = "TEXT",
    val content: String,
    val status: String = "SENT",
    val timestamp: Long = System.currentTimeMillis(),
    val sequenceNumber: Long = 0,
    val replyToId: String? = null,
    val isEdited: Boolean = false,
    val metadata: String? = null
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val name: String,
    val avatarUrl: String? = null,
    val ownerId: String,
    val myRole: String = "MEMBER",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val remoteId: String,
    val type: String = "VOIP_VIDEO",
    val direction: String = "OUTGOING",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0,
    val dinstarSlot: Int? = null
)

@Dao
interface RedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :cId ORDER BY sequenceNumber ASC")
    fun getMessages(cId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int = 100): Flow<List<MessageEntity>>

    @Query("SELECT COALESCE(MAX(sequenceNumber),0) FROM messages WHERE conversationId = :cId")
    suspend fun getLastSequenceNumber(cId: String): Long

    @Query("SELECT * FROM messages WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): MessageEntity?

    @Query("UPDATE messages SET status = :status WHERE uuid = :uuid")
    suspend fun updateStatus(uuid: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Query("SELECT * FROM groups")
    fun getGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallLogEntity)

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getCalls(): Flow<List<CallLogEntity>>

    @Query("DELETE FROM messages WHERE timestamp < :before")
    suspend fun cleanupOld(before: Long)
}

@Database(entities = [MessageEntity::class, GroupEntity::class, CallLogEntity::class], version = 2, exportSchema = false)
abstract class RedMasterDatabase : RoomDatabase() {
    abstract fun dao(): RedDao
}
