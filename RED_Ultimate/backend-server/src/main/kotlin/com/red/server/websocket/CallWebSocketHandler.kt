package com.red.server.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * Routes WebRTC signaling envelopes. Media remains end-to-end WebRTC/SFU;
 * this handler never inspects SDP content beyond routing metadata.
 */
@Component
class CallWebSocketHandler(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val signal = objectMapper.readValue(message.payload, CallSignal::class.java)
        require(signal.targetUserId.isNotBlank()) { "targetUserId is required" }
        sessions[signal.targetUserId]
            ?.takeIf { it.isOpen }
            ?.sendMessage(message)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? String ?: return
        sessions[userId] = session
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: org.springframework.web.socket.CloseStatus
    ) {
        sessions.entries.removeIf { it.value.id == session.id }
    }
}

data class CallSignal(
    val targetUserId: String = "",
    val type: String = "",
    val payload: Map<String, Any?> = emptyMap()
)
