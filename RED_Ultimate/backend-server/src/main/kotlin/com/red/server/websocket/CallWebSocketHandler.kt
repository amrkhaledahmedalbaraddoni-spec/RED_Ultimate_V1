package com.red.server.websocket

import com.red.sovereign.proto.CallProtos
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * RED Master Call REDer
 * Connects Caller to Callee and SFU.
 */
class CallWebSocketHandler : BinaryWebSocketHandler() {
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val signal = CallProtos.CallRED.parseFrom(message.payload.array())
        val targetSession = sessions[signal.targetUserId]

        if (targetSession != null && targetSession.isOpen) {
            // Forward the binary signal to the target user (System A Flow)
            targetSession.sendMessage(BinaryMessage(message.payload.array()))
            println("🔴 RED: REDing ${signal.type} forwarded to ${signal.targetUserId}")
        } else {
            println("⚠️ RED: Target user ${signal.targetUserId} is offline.")
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as String
        sessions[userId] = session
    }
}
