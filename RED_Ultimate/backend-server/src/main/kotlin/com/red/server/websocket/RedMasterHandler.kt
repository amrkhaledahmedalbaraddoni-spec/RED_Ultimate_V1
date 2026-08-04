package com.red.server.websocket

import com.google.protobuf.ByteString
import com.red.server.database.RedisManager
import com.red.server.messaging.DeleteService
import com.red.server.messaging.MessageService
import com.red.sovereign.proto.RedProtos
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class RedMasterHandler(
    private val messageService: MessageService,
    private val deleteService: DeleteService,
    private val redisManager: RedisManager
) : BinaryWebSocketHandler() {
    private val activeSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val signal = RedProtos.RedRED.parseFrom(message.payload)
        when (signal.signalCase) {
            RedProtos.RedRED.SignalCase.MESSAGE -> handleIncomingMessage(session, signal.message)
            RedProtos.RedRED.SignalCase.TYPING -> handleTyping(session, signal.typing)
            RedProtos.RedRED.SignalCase.SYNC_REQ -> handleSync(session, signal.syncReq)
            RedProtos.RedRED.SignalCase.DELETE -> handleDelete(session, signal.delete)
            else -> Unit
        }
    }

    private fun handleIncomingMessage(session: WebSocketSession, message: RedProtos.ChatMessage) {
        val authenticatedUserId = requireUserId(session)
        require(message.senderId == authenticatedUserId) { "senderId does not match authenticated user" }

        val stored = messageService.processIncoming(message)
        val ack = RedProtos.RedRED.newBuilder().setAck(
            RedProtos.MessageAck.newBuilder()
                .setMessageId(message.id)
                .setSequenceNumber(stored.sequenceNumber)
                .setStatus("SENT")
        ).build()
        session.sendMessage(BinaryMessage(ack.toByteArray()))

        activeSessions[message.receiverId]
            ?.takeIf { it.isOpen }
            ?.sendMessage(BinaryMessage(RedProtos.RedRED.newBuilder().setMessage(message).build().toByteArray()))
    }

    private fun handleTyping(session: WebSocketSession, typing: RedProtos.TypingRED) {
        require(typing.userId == requireUserId(session)) { "userId does not match authenticated user" }
        redisManager.setTyping(typing.userId, typing.conversationId)
    }

    private fun handleSync(session: WebSocketSession, request: RedProtos.SyncRequest) {
        messageService.getMissedMessages(
            request.conversationId,
            request.fromSequence,
            request.toSequence
        ).forEach { stored ->
            val message = RedProtos.ChatMessage.newBuilder()
                .setId(stored.uuid)
                .setConversationId(stored.conversationId)
                .setSenderId(stored.senderId)
                .setReceiverId(stored.receiverId)
                .setPayload(ByteString.copyFrom(stored.payload))
                .setTimestamp(stored.createdAt.toEpochMilli())
                .setSequenceNumber(stored.sequenceNumber)
                .setType(stored.messageType)
                .build()
            session.sendMessage(
                BinaryMessage(RedProtos.RedRED.newBuilder().setMessage(message).build().toByteArray())
            )
        }
    }

    private fun handleDelete(session: WebSocketSession, delete: RedProtos.DeleteRED) {
        if (!delete.forEveryone) return
        val deleted = deleteService.deleteForEveryone(delete.messageId, requireUserId(session))
        if (deleted) {
            val envelope = BinaryMessage(
                RedProtos.RedRED.newBuilder().setDelete(delete).build().toByteArray()
            )
            activeSessions.values.filter { it.isOpen }.forEach { it.sendMessage(envelope) }
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        activeSessions[requireUserId(session)] = session
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        activeSessions.entries.removeIf { it.value.id == session.id }
    }

    private fun requireUserId(session: WebSocketSession): String =
        session.attributes["userId"] as? String ?: error("Authenticated user is missing")
}
