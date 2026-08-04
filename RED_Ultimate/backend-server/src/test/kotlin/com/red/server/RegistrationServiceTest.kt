package com.red.server

import com.red.server.auth.RedIdGenerator
import com.red.server.auth.RegisterRequest
import com.red.server.auth.RegistrationService
import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.UserAccount
import com.red.server.auth.repository.UserAccountRepository
import com.red.server.auth.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder

class RegistrationServiceTest {
    private val users = mock(UserAccountRepository::class.java)
    private val encoder = mock(PasswordEncoder::class.java)
    private val redIds = mock(RedIdGenerator::class.java)
    private val jwt = mock(JwtService::class.java)
    private val service = RegistrationService(users, encoder, redIds, jwt)

    @Test
    fun `new account is pending and receives no token`() {
        `when`(users.existsByUsernameIgnoreCase("ahmed.red")).thenReturn(false)
        `when`(redIds.next()).thenReturn("RED-7K4M-82QX")
        `when`(encoder.encode("a-strong-password")).thenReturn("argon2-hash")
        `when`(users.saveAndFlush(any(UserAccount::class.java))).thenAnswer { it.arguments[0] }

        val result = service.register(
            RegisterRequest("Ahmed.Red", "a-strong-password", "أحمد")
        )

        assertEquals(AccountStatus.PENDING, result.status)
        assertEquals("RED-7K4M-82QX", result.user.redId)
        assertEquals("ahmed.red", result.user.username)
        assertNull(result.accessToken)
        assertEquals("ACCOUNT_PENDING_ADMIN_APPROVAL", result.message)
    }

    @Test
    fun `phone-like or malformed username is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.register(RegisterRequest("777123456", "a-strong-password", "أحمد"))
        }
    }
}
