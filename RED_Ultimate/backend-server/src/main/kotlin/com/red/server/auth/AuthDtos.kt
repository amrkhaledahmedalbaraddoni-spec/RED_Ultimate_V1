package com.red.server.auth

import com.red.server.auth.model.AccountRole
import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.UserAccount
import java.time.Instant
import java.util.UUID

data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class ApprovalActionRequest(
    val userId: UUID,
    val action: AccountStatus,
    val reason: String? = null
)

data class UserAccountResponse(
    val id: UUID,
    val redId: String,
    val username: String,
    val displayName: String,
    val status: AccountStatus,
    val role: AccountRole,
    val createdAt: Instant,
    val updatedAt: Instant,
    val rejectionReason: String?
)

data class AuthResponse(
    val status: AccountStatus,
    val user: UserAccountResponse,
    val accessToken: String? = null,
    val tokenType: String? = null,
    val expiresInSeconds: Long? = null,
    val message: String? = null
)

fun UserAccount.toResponse() = UserAccountResponse(
    id = id,
    redId = redId,
    username = username,
    displayName = displayName,
    status = status,
    role = role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    rejectionReason = rejectionReason
)
