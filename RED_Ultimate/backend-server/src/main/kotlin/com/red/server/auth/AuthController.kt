package com.red.server.auth

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val approvalService: RedApprovalService
) {
    @PostMapping("/register")
    fun register(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val email = data["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "email required"))
        val name = data["fullName"] ?: data["name"] ?: data["full_name"]
        val phone = data["phone"]

        val user = approvalService.registerUser(email, name, phone)
        return ResponseEntity.ok(mapOf(
            "status" to user.status,
            "userId" to user.id,
            "email" to user.email,
            "message" to "Waiting for Admin Approval - RED Sovereign"
        ))
    }

    @PostMapping("/login")
    fun login(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val email = data["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "email required"))
        val password = data["password"] // In production verify BCrypt

        val user = approvalService.getUserByEmail(email)
            ?: return ResponseEntity.status(401).body(mapOf("error" to "NOT_FOUND", "message" to "User not found"))

        return when (user.status) {
            "APPROVED" -> ResponseEntity.ok(mapOf(
                "token" to "red-jwt-${UUID.randomUUID()}-${user.id}",
                "userId" to user.id,
                "redId" to (user.redId ?: "RED-${user.id.take(4)}"),
                "email" to user.email,
                "role" to user.role,
                "status" to "OK",
                "gsmNumber" to (user.gsmNumber ?: ""),
                "message" to "RED Sovereign Login Successful"
            ))
            "PENDING" -> ResponseEntity.status(403).body(mapOf("error" to "PENDING_APPROVAL", "message" to "Account pending admin approval", "userId" to user.id))
            "BANNED" -> ResponseEntity.status(403).body(mapOf("error" to "ACCOUNT_BANNED", "message" to "Account has been banned by admin"))
            "REJECTED" -> ResponseEntity.status(403).body(mapOf("error" to "ACCOUNT_REJECTED", "message" to "Account rejected"))
            else -> ResponseEntity.status(401).body(mapOf("error" to "UNKNOWN_STATUS"))
        }
    }

    @PostMapping("/admin/approve")
    fun approveUser(@RequestParam email: String): ResponseEntity<Any> {
        val user = approvalService.approveByEmail(email)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapOf("message" to "User $email approved", "user" to user))
    }

    @GetMapping("/status/{email}")
    fun checkStatus(@PathVariable email: String): ResponseEntity<Any> {
        val user = approvalService.getUserByEmail(email)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapOf(
            "email" to user.email,
            "status" to user.status,
            "redId" to user.redId,
            "role" to user.role
        ))
    }
}
