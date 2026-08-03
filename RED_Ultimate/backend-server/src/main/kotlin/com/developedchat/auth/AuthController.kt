package com.red.auth

import com.red.core.models.User
import com.red.core.models.UserStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/auth")
class AuthController {

    // Simple in-memory store for MVP demonstration
    private val users = ConcurrentHashMap<String, User>()

    @PostMapping("/register")
    fun register(@RequestBody request: Map<String, String>): ResponseEntity<Any> {
        val email = request["email"] ?: return ResponseEntity.badRequest().body("Email required")
        val name = request["name"] ?: return ResponseEntity.badRequest().body("Name required")
        
        val newUser = User(
            id = java.util.UUID.randomUUID().toString(),
            email = email,
            name = name,
            avatarUrl = null,
            status = UserStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        users[email] = newUser
        return ResponseEntity.ok(mapOf("status" to "PENDING", "user" to newUser))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: Map<String, String>): ResponseEntity<Any> {
        val email = request["email"] ?: return ResponseEntity.badRequest().body("Email required")
        val user = users[email] ?: return ResponseEntity.status(401).body("User not found")
        
        return ResponseEntity.ok(mapOf("token" to "mock-jwt-token", "user" to user))
    }

    @GetMapping("/status")
    fun getStatus(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        // In real app, extract user from JWT. Here we mock it.
        return ResponseEntity.ok(mapOf("status" to UserStatus.PENDING))
    }
}

@RestController
@RequestMapping("/api/admin")
class AdminController {
    @PostMapping("/approve/{userId}")
    fun approve(@PathVariable userId: String): ResponseEntity<Any> {
        // Find user and set status to APPROVED
        return ResponseEntity.ok(mapOf("message" to "User approved"))
    }
}
