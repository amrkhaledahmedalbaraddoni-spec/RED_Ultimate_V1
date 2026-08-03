package com.red.server.websocket

import com.red.sovereign.proto.ChatProtos
import com.red.server.messaging.MessageService
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(private val messageService: MessageService) : BinaryWebSocketHandler() {
    
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val payload = message.payload.array()
        val chatMsg = ChatProtos.ChatMessage.parseFrom(payload)

        // 1. معالجة الرسالة (Dedup + Sequence + Save)
        val sequenceNumber = messageService.processIncoming(chatMsg)
        
        // 2. إرسال ACK فوراً للمرسل
        if (sequenceNumber != -1L) {
            sendAck(session, chatMsg.id, "SENT", sequenceNumber)
            
            // 3. التوجيه للمستلم
            val recipientSession = sessions[chatMsg.receiverId]
            if (recipientSession != null && recipientSession.isOpen) {
                recipientSession.sendMessage(BinaryMessage(payload))
            }
        }
    }

    private fun sendAck(session: WebSocketSession, msgId: String, status: String, seq: Long) {
        val ack = ChatProtos.MessageAck.newBuilder()
            .setMessageId(msgId)
            .setStatus(status)
            .setSequenceNumber(seq)
            .build()
        session.sendMessage(BinaryMessage(ack.toByteArray()))
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? String
        if (userId != null) sessions[userId] = session
    }
}
