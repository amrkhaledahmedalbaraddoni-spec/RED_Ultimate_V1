package com.red.server.auth

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/auth")
class AuthController {

    // محاكاة لقاعدة بيانات المستخدمين وحالاتهم
    private val userDatabase = ConcurrentHashMap<String, String>() // Email -> Status

    @PostMapping("/register")
    fun register(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val email = data["email"] ?: return ResponseEntity.badRequest().build()
        userDatabase[email] = "PENDING"
        return ResponseEntity.ok(mapOf("status" to "PENDING", "message" to "Waiting for Admin Approval"))
    }

    @PostMapping("/login")
    fun login(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val email = data["email"] ?: return ResponseEntity.badRequest().build()
        val status = userDatabase[email] ?: "NOT_FOUND"

        return when (status) {
            "APPROVED" -> ResponseEntity.ok(mapOf("token" to "red-jwt-${java.util.UUID.randomUUID()}", "status" to "OK"))
            "PENDING" -> ResponseEntity.status(403).body(mapOf("error" to "PENDING_APPROVAL"))
            "BANNED" -> ResponseEntity.status(403).body(mapOf("error" to "ACCOUNT_BANNED"))
            else -> ResponseEntity.status(401).build()
        }
    }

    @PostMapping("/admin/approve")
    fun approveUser(@RequestParam email: String): ResponseEntity<Any> {
        userDatabase[email] = "APPROVED"
        return ResponseEntity.ok(mapOf("message" to "User $email approved"))
    }
}
