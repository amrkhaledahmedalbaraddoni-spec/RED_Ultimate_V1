package com.red.server.auth.security

import com.red.server.auth.model.AccountStatus
import com.red.server.auth.repository.UserAccountRepository
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class JwtHandshakeInterceptor(
    private val jwtService: JwtService,
    private val users: UserAccountRepository
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = request.headers.getFirst("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')

        if (token.isNullOrBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        val user = runCatching { users.findById(jwtService.userId(token)).orElse(null) }.getOrNull()
        if (user == null || user.status != AccountStatus.APPROVED) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        attributes["userId"] = user.redId
        attributes["accountId"] = user.id.toString()
        attributes["redId"] = user.redId
        attributes["role"] = user.role.name
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) = Unit
}
