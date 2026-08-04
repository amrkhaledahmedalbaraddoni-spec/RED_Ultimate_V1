package com.red.server.websocket

import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList

@Component
class AdminLogHandler : TextWebSocketHandler() {
    
    private val adminSessions = CopyOnWriteArrayList<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        if (session.attributes["role"] != "ADMIN") {
            session.close(org.springframework.web.socket.CloseStatus.POLICY_VIOLATION)
            return
        }
        adminSessions.add(session)
        session.sendMessage(TextMessage("RED admin log stream connected"))
    }

    /**
     * وظيفة مركزية لبث الأحداث من أي مكان في السيرفر إلى لوحة التحكم
     */
    fun broadcastLog(system: String, message: String) {
        val payload = "[${system}] ${message}"
        adminSessions.forEach { if (it.isOpen) it.sendMessage(TextMessage(payload)) }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        adminSessions.remove(session)
    }
}
