package com.red.server.config

import com.red.server.websocket.AdminLogHandler
import com.red.server.websocket.ChatWebSocketHandler
import com.red.server.websocket.RedMasterHandler
import com.red.server.websocket.RedWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * WebSocket Configuration — registers all WebSocket handlers at their endpoints.
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val redMasterHandler: RedMasterHandler,
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val redWebSocketHandler: RedWebSocketHandler,
    private val adminLogHandler: AdminLogHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // RED Master Protocol
        registry.addHandler(redMasterHandler, "/ws/master")
            .setAllowedOrigins("*")

        // Chat WebSocket
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .setAllowedOrigins("*")

        // RED WebSocket (general)
        registry.addHandler(redWebSocketHandler, "/ws/red")
            .setAllowedOrigins("*")

        // Admin Logs WebSocket
        registry.addHandler(adminLogHandler, "/ws/admin/logs")
            .setAllowedOrigins("*")
    }
}
