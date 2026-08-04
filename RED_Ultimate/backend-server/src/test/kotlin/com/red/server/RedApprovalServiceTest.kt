package com.red.server

import com.red.server.auth.RedApprovalService
import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.UserAccount
import com.red.server.auth.repository.UserAccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class RedApprovalServiceTest {
    private val users = mock(UserAccountRepository::class.java)
    private val service = RedApprovalService(users)

    @Test
    fun `admin approval changes pending account and records approver`() {
        val user = UserAccount(redId = "RED-TEST-0001", username = "test.user", displayName = "Test")
        val adminId = UUID.randomUUID()
        `when`(users.findById(user.id)).thenReturn(Optional.of(user))
        `when`(users.save(any(UserAccount::class.java))).thenAnswer { it.arguments[0] }

        val result = service.processAction(user.id, AccountStatus.APPROVED, adminId = adminId)

        assertEquals(AccountStatus.APPROVED, result.status)
        assertEquals(adminId, user.approvedBy)
        assertNotNull(user.approvedAt)
    }
}
