package com.red.server.config

import com.red.server.auth.security.JwtHandshakeInterceptor
import com.red.server.websocket.AdminLogHandler
import com.red.server.websocket.CallWebSocketHandler
import com.red.server.websocket.RedMasterHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val redMasterHandler: RedMasterHandler,
    private val callWebSocketHandler: CallWebSocketHandler,
    private val adminLogHandler: AdminLogHandler,
    private val jwtHandshakeInterceptor: JwtHandshakeInterceptor
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // The only messaging protocol. Android and backend both use RedProtos.RedRED.
        registry.addHandler(redMasterHandler, "/ws/master")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*")

        // WebRTC signaling only; media is handled by WebRTC/mediasoup.
        registry.addHandler(callWebSocketHandler, "/ws/calls")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*")

        registry.addHandler(adminLogHandler, "/ws/admin/logs")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}
