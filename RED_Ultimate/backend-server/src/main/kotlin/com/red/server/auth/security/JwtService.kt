package com.red.server.auth.security

import com.red.server.auth.model.UserAccount
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${red.security.jwt-secret}") private val configuredSecret: String,
    @Value("\${red.security.jwt-expiration-ms:3600000}") private val expirationMs: Long
) {
    private val key: SecretKey by lazy {
        require(configuredSecret != "change-me-in-production-please") {
            "JWT_SECRET must be configured; the insecure development value is not allowed"
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(configuredSecret.toByteArray(StandardCharsets.UTF_8))
        Keys.hmacShaKeyFor(digest)
    }

    fun issue(user: UserAccount): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("redId", user.redId)
            .claim("username", user.username)
            .claim("role", user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload

    fun userId(token: String): UUID = UUID.fromString(parse(token).subject)

    fun expirationSeconds(): Long = expirationMs / 1000
}
