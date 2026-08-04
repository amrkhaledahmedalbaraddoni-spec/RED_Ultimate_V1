package com.red.server.auth.security

import com.red.server.auth.model.AccountRole
import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.DeviceStatus
import com.red.server.auth.repository.UserAccountRepository
import com.red.server.auth.repository.UserDeviceRepository
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class JwtHandshakeInterceptor(
    private val jwtService: JwtService,
    private val users: UserAccountRepository,
    private val devices: UserDeviceRepository
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val headerToken = request.headers.getFirst("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
        // Browser WebSocket APIs cannot set Authorization headers. Query fallback is for the
        // local admin dashboard and must only be used over WireGuard or TLS.
        val queryToken = request.uri.rawQuery
            ?.split('&')
            ?.firstOrNull { it.startsWith("access_token=") }
            ?.substringAfter('=')
            ?.let { java.net.URLDecoder.decode(it, Charsets.UTF_8) }
        val token = headerToken ?: queryToken
        if (token.isNullOrBlank()) return reject(response)

        val result = runCatching {
            val user = users.findById(jwtService.userId(token)).orElse(null) ?: return@runCatching null
            val deviceId = jwtService.deviceId(token)
            val deviceAllowed = if (deviceId != null) {
                devices.findByIdAndUserId(deviceId, user.id)?.status == DeviceStatus.APPROVED
            } else user.role == AccountRole.ADMIN
            if (user.status != AccountStatus.APPROVED || !deviceAllowed) null else user to deviceId
        }.getOrNull() ?: return reject(response)

        val (user, deviceId) = result
        attributes["userId"] = user.redId
        attributes["accountId"] = user.id.toString()
        attributes["redId"] = user.redId
        attributes["role"] = user.role.name
        if (deviceId != null) attributes["deviceId"] = deviceId.toString()
        return true
    }

    private fun reject(response: ServerHttpResponse): Boolean {
        response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) = Unit
}
