package com.red.server.websocket

import com.red.sovereign.proto.RedProtos
import com.red.server.messaging.MessageService
import com.red.server.database.RedisManager
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RedMasterHandler(
    private val messageService: MessageService,
    private val redisManager: RedisManager
) : BinaryWebSocketHandler() {
    
    private val activeSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val signal = RedProtos.RedRED.parseFrom(message.payload.array())
        
        when (signal.signalCase) {
            RedProtos.RedRED.REDCase.MESSAGE -> handleIncomingMessage(session, signal.message)
            RedProtos.RedRED.REDCase.TYPING -> handleTyping(signal.typing)
            RedProtos.RedRED.REDCase.SYNC_REQ -> handleSync(session, signal.syncReq)
            else -> println("🔴 RED: Unknown signal type")
        }
    }

    private fun handleIncomingMessage(session: WebSocketSession, msg: RedProtos.ChatMessage) {
        // 1. خوارزمية التوصيل المضمونة
        val seq = messageService.processIncoming(msg)
        if (seq != -1L) {
            // 2. رد التأكيد ACK
            val ack = RedProtos.RedRED.newBuilder().setAck(
                RedProtos.MessageAck.newBuilder().setMessageId(msg.id).setSequenceNumber(seq).setStatus("SENT")
            ).build()
            session.sendMessage(BinaryMessage(ack.toByteArray()))

            // 3. التوجيه للمستلم
            activeSessions[msg.receiverId]?.let { 
                it.sendMessage(BinaryMessage(RedProtos.RedRED.newBuilder().setMessage(msg).build().toByteArray()))
            }
        }
    }

    private fun handleTyping(typing: RedProtos.TypingRED) {
        // نشر الحالة عبر Redis لتصل لكل الأجهزة المشتركة
        redisManager.setTyping(typing.userId, typing.conversationId)
    }

    private fun handleSync(session: WebSocketSession, req: RedProtos.SyncRequest) {
        // جلب الرسائل المفقودة من MongoDB وإرسالها
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? String ?: return
        activeSessions[userId] = session
    }
}
