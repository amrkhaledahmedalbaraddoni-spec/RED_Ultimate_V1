package com.red.server.config

import com.red.server.websocket.*
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * RED Ultimate WebSocket Config - System C + Live Diagnostics
 * Registers all real-time handlers
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val redMasterHandler: RedMasterHandler,
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val redWebSocketHandler: RedWebSocketHandler,
    private val adminLogHandler: AdminLogHandler,
    private val callWebSocketHandler: CallWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // System C - RED Master Protocol (Binary ProtoBuf)
        registry.addHandler(redMasterHandler, "/ws/master")
            .setAllowedOrigins("*")
            .withSockJS().setClientLibraryUrl("")

        registry.addHandler(redMasterHandler, "/ws/master-native")
            .setAllowedOrigins("*")

        // Chat - Guaranteed Delivery + Typing + Sync
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .setAllowedOrigins("*")

        // RED General - Legacy Support
        registry.addHandler(redWebSocketHandler, "/ws/red")
            .setAllowedOrigins("*")

        // Admin Live Logs - Real-time Diagnostics
        registry.addHandler(adminLogHandler, "/ws/admin/logs")
            .setAllowedOrigins("*")

        // Calls - System A (WebRTC Signaling) + System B (PSTN Status)
        registry.addHandler(callWebSocketHandler, "/ws/calls")
            .setAllowedOrigins("*")

        registry.addHandler(callWebSocketHandler, "/ws/call")
            .setAllowedOrigins("*")
    }
}
