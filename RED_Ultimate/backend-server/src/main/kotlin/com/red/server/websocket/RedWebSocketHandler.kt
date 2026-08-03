package com.red.server.websocket

import com.red.proto.ChatProtos
import com.red.server.messaging.MessageService
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RedWebSocketHandler(private val messageService: MessageService) : BinaryWebSocketHandler() {
    
    private val activeSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val bytes = message.payload.array()
        
        try {
            val chatMsg = ChatProtos.ChatMessage.parseFrom(bytes)
            
            // 1. خوارزمية التوصيل: (Dedup + Sequence + Save)
            val seq = messageService.processIncoming(chatMsg)
            
            if (seq != -1L) {
                // 2. إرسال ACK فوري للمرسل
                sendAck(session, chatMsg.id, seq)

                // 3. التوجيه للمستلم
                val target = activeSessions[chatMsg.receiverId]
                if (target != null && target.isOpen) {
                    target.sendMessage(BinaryMessage(bytes))
                }
            }
        } catch (e: Exception) {
            println("⚠️ RED: Error parsing ProtoBuf message: ${e.message}")
        }
    }

    private fun sendAck(session: WebSocketSession, msgId: String, seq: Long) {
        val ack = ChatProtos.MessageAck.newBuilder()
            .setMessageId(msgId)
            .setSequenceNumber(seq)
            .setStatus("SENT")
            .build()
        session.sendMessage(BinaryMessage(ack.toByteArray()))
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? String ?: "guest"
        activeSessions[userId] = session
    }
}
