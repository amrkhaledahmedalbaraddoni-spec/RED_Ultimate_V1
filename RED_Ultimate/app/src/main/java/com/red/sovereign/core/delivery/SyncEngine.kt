package com.red.sovereign.core.delivery

import com.red.sovereign.core.database.RedDao
import com.red.sovereign.core.network.RedWebSocketClient
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val redDao: RedDao,
    private val webSocketClient: RedWebSocketClient
) {
    /**
     * Gap repair: if local max is 7 and remote is 10, request 8,9
     */
    suspend fun repairSequenceGaps(conversationId: String, currentMaxSeq: Long) {
        try {
            // Get last sequence from DB
            // Simplified: flow first
            val messages = redDao.getMessages(conversationId).firstOrNull() ?: emptyList()
            val localMax = messages.maxOfOrNull { it.sequenceNumber } ?: 0L
            if (currentMaxSeq > localMax + 1) {
                val syncReq = """{"type":"sync","conversationId":"$conversationId","from":${localMax + 1},"to":$currentMaxSeq}"""
                webSocketClient.send(syncReq.toByteArray())
                println("🔴 RED Sync: Requesting gap ${localMax + 1}..$currentMaxSeq for $conversationId")
            }
        } catch (e: Exception) {
            println("⚠️ RED Sync gap check failed: ${e.message}")
        }
    }

    suspend fun requestFullSync(conversationId: String) {
        val req = """{"type":"sync_full","conversationId":"$conversationId"}"""
        webSocketClient.send(req.toByteArray())
    }
}
