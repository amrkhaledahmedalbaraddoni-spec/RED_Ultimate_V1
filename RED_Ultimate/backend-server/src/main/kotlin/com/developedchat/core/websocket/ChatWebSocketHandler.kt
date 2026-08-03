package com.red.core.websocket

import com.red.proto.ChatProtos
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler : BinaryWebSocketHandler() {

    // إدارة الجلسات النشطة: UserID -> Session
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    
    // منع التكرار (UUID Cache)
    private val processedMessages = ConcurrentHashMap.newKeySet<String>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val payload = message.payload.array()
        val chatMsg = ChatProtos.ChatMessage.parseFrom(payload)

        // 1. إرسال ACK فوراً للمرسل (Algorithm Step 4)
        sendAck(session, chatMsg.id)

        // 2. التحقق من التكرار (Dedup)
        if (processedMessages.contains(chatMsg.id)) return
        processedMessages.add(chatMsg.id)

        // 3. تخزين في MongoDB (Offline Storage)
        saveToMongo(chatMsg)

        // 4. محاولة الإرسال للمستلم
        val recipientSession = sessions[chatMsg.receiver_id]
        if (recipientSession != null && recipientSession.isOpen) {
            recipientSession.sendMessage(BinaryMessage(payload))
        } else {
            // إذا كان المستلم أوفلاين -> إرسال إشعار Push (UnifiedPush)
            sendPushNotification(chatMsg.receiver_id, "New Message")
        }
    }

    private fun sendAck(session: WebSocketSession, messageId: String) {
        val ack = ChatProtos.MessageAck.newBuilder()
            .setMessageId(messageId)
            .setStatus(ChatProtos.AckStatus.SENT)
            .build()
        session.sendMessage(BinaryMessage(ack.toByteArray()))
    }

    private fun saveToMongo(msg: ChatProtos.ChatMessage) {
        // Logic to persist in MongoDB
        println("Message ${msg.id} saved to MongoDB with Seq: ${msg.sequence_number}")
    }

    private fun sendPushNotification(userId: String, content: String) {
        // Local Push Server trigger
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? String
        if (userId != null) sessions[userId] = session
    }
}
