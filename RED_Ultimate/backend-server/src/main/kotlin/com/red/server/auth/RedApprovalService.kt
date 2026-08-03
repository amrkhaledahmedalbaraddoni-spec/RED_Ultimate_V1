package com.red.server.auth

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant
import java.util.UUID

data class RedUser(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String? = null,
    val phone: String? = null,
    var status: String = "PENDING", // PENDING, APPROVED, BANNED, REJECTED
    var role: String = "USER", // USER, ADMIN, SUPER_ADMIN
    val createdAt: Instant = Instant.now(),
    var approvedAt: Instant? = null,
    var lastSeen: Instant? = null,
    val redId: String? = null, // Sovereign ID assigned by admin
    val gsmNumber: String? = null
)

@Service
class RedApprovalService {

    // In-Memory Authority Storage (Production would use PostgreSQL JPA)
    // Key: userId or email -> User
    private val users = ConcurrentHashMap<String, RedUser>()
    private val emailToId = ConcurrentHashMap<String, String>()

    init {
        // Seed super admin
        val admin = RedUser(
            id = "admin-001",
            email = "admin@red.sovereign",
            fullName = "RED Master Admin",
            status = "APPROVED",
            role = "SUPER_ADMIN",
            redId = "RED-0001"
        )
        users[admin.id] = admin
        emailToId[admin.email] = admin.id
        println("🔴 RED Authority: Super Admin seeded ${admin.email}")
    }

    // ===== Core Authority Operations =====

    fun registerUser(email: String, fullName: String?, phone: String? = null): RedUser {
        val existingId = emailToId[email]
        if (existingId != null) return users[existingId]!!

        val user = RedUser(
            email = email,
            fullName = fullName,
            phone = phone,
            status = "PENDING"
        )
        users[user.id] = user
        emailToId[email] = user.id
        println("🔴 RED Auth: New registration PENDING $email (${user.id})")
        return user
    }

    fun getPendingList(): List<RedUser> {
        return users.values.filter { it.status == "PENDING" }.sortedBy { it.createdAt }
    }

    fun getApprovedUsers(): List<RedUser> {
        return users.values.filter { it.status == "APPROVED" }.sortedByDescending { it.approvedAt }
    }

    fun getAllUsers(): List<RedUser> = users.values.sortedBy { it.createdAt }

    fun getUserById(userId: String): RedUser? = users[userId]
    fun getUserByEmail(email: String): RedUser? = emailToId[email]?.let { users[it] }

    fun approveUser(userId: String): RedUser? {
        val user = users[userId] ?: return null
        user.status = "APPROVED"
        user.approvedAt = Instant.now()
        // Assign RED ID and GSM if not present
        val updated = user.copy(
            redId = user.redId ?: "RED-${(1000..9999).random()}",
            status = "APPROVED",
            approvedAt = Instant.now()
        )
        users[userId] = updated
        println("✅ RED Admin: APPROVED ${updated.email} -> ${updated.redId}")
        return updated
    }

    fun approveByEmail(email: String): RedUser? {
        val id = emailToId[email] ?: return null
        return approveUser(id)
    }

    fun banUser(userId: String): RedUser? {
        val user = users[userId] ?: return null
        val updated = user.copy(status = "BANNED")
        users[userId] = updated
        println("🚫 RED Admin: BANNED ${updated.email}")
        return updated
    }

    fun rejectUser(userId: String): RedUser? {
        val user = users[userId] ?: return null
        val updated = user.copy(status = "REJECTED")
        users[userId] = updated
        println("❌ RED Admin: REJECTED ${updated.email}")
        return updated
    }

    fun isAllowed(userId: String): Boolean {
        return users[userId]?.status == "APPROVED" || users[userId]?.role == "SUPER_ADMIN"
    }

    fun isAllowedEmail(email: String): Boolean {
        val id = emailToId[email] ?: return false
        return isAllowed(id)
    }

    // Unified action processor for Master Dashboard tabs
    fun processAction(userId: String, action: String): Map<String, Any> {
        return when (action.uppercase()) {
            "APPROVED", "APPROVE" -> {
                val u = approveUser(userId)
                mapOf("status" to "SUCCESS", "action" to "APPROVED", "user" to (u ?: "NOT_FOUND"))
            }
            "BANNED", "BAN" -> {
                val u = banUser(userId)
                mapOf("status" to "SUCCESS", "action" to "BANNED", "user" to (u ?: "NOT_FOUND"))
            }
            "REJECTED", "REJECT" -> {
                val u = rejectUser(userId)
                mapOf("status" to "SUCCESS", "action" to "REJECTED", "user" to (u ?: "NOT_FOUND"))
            }
            "DELETE" -> {
                val removed = users.remove(userId)
                removed?.let { emailToId.remove(it.email) }
                mapOf("status" to "DELETED", "userId" to userId)
            }
            else -> mapOf("status" to "UNKNOWN_ACTION", "action" to action)
        }
    }

    fun getStats(): Map<String, Any> {
        return mapOf(
            "total" to users.size,
            "pending" to users.values.count { it.status == "PENDING" },
            "approved" to users.values.count { it.status == "APPROVED" },
            "banned" to users.values.count { it.status == "BANNED" },
            "admins" to users.values.count { it.role.contains("ADMIN") }
        )
    }
}
