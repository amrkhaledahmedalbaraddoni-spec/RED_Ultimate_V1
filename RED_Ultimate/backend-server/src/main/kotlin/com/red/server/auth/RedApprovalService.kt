package com.red.server.auth

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RedApprovalService {
    // تخزين محلي لقرارات المدير (Approved Users Only)
    private val approvedUsers = ConcurrentHashMap<String, Boolean>()

    fun approveUser(userId: String) {
        approvedUsers[userId] = true
        println("🔴 RED Admin: User $userId has been APPROVED.")
    }

    fun isAllowed(userId: String): Boolean {
        return approvedUsers.getOrDefault(userId, false)
    }

    fun banUser(userId: String) {
        approvedUsers[userId] = false
        println("🚫 RED Admin: User $userId has been BANNED.")
    }
}
