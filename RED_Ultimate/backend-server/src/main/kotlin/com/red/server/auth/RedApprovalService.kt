package com.red.server.auth

import com.red.server.auth.model.AccountStatus
import com.red.server.auth.repository.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RedApprovalService(private val users: UserAccountRepository) {

    @Transactional(readOnly = true)
    fun getPendingList(): List<UserAccountResponse> =
        users.findAllByStatusOrderByCreatedAtAsc(AccountStatus.PENDING).map { it.toResponse() }

    @Transactional
    fun processAction(
        userId: UUID,
        action: AccountStatus,
        reason: String? = null,
        adminId: UUID? = null
    ): UserAccountResponse {
        require(action in allowedAdminActions) { "Unsupported account action: $action" }
        val user = users.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        require(user.role.name != "ADMIN" || action == AccountStatus.APPROVED) {
            "Administrator accounts cannot be blocked through this endpoint"
        }

        user.status = action
        user.updatedAt = Instant.now()
        user.rejectionReason = reason?.trim()?.takeIf { it.isNotEmpty() }
        if (action == AccountStatus.APPROVED) {
            user.approvedAt = Instant.now()
            user.approvedBy = adminId
            user.rejectionReason = null
        }
        return users.save(user).toResponse()
    }

    fun processAction(userId: String, action: String): UserAccountResponse =
        processAction(UUID.fromString(userId), AccountStatus.valueOf(action.uppercase()))

    fun approveUser(userId: String): UserAccountResponse =
        processAction(UUID.fromString(userId), AccountStatus.APPROVED)

    fun rejectUser(userId: String): UserAccountResponse =
        processAction(UUID.fromString(userId), AccountStatus.REJECTED)

    fun banUser(userId: String): UserAccountResponse =
        processAction(UUID.fromString(userId), AccountStatus.BANNED)

    @Transactional(readOnly = true)
    fun isAllowed(userId: String): Boolean =
        runCatching { users.findById(UUID.fromString(userId)).orElse(null)?.status == AccountStatus.APPROVED }
            .getOrDefault(false)

    companion object {
        private val allowedAdminActions = setOf(
            AccountStatus.APPROVED,
            AccountStatus.REJECTED,
            AccountStatus.SUSPENDED,
            AccountStatus.BANNED
        )
    }
}
