package com.red.server.auth

import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.UserAccount
import com.red.server.auth.repository.UserAccountRepository
import com.red.server.auth.security.JwtService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegistrationService(
    private val users: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val redIdGenerator: RedIdGenerator,
    private val jwtService: JwtService
) {
    private val usernamePattern = Regex("^[a-zA-Z][a-zA-Z0-9_.]{2,31}$")

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val username = request.username.trim().lowercase()
        val displayName = request.displayName.trim()

        require(usernamePattern.matches(username)) {
            "Username must be 3-32 characters and contain only letters, numbers, dot or underscore"
        }
        require(displayName.length in 2..100) { "Display name must be 2-100 characters" }
        require(request.password.length >= 10) { "Password must contain at least 10 characters" }
        require(!users.existsByUsernameIgnoreCase(username)) { "Username is already registered" }

        val user = try {
            users.saveAndFlush(
                UserAccount(
                    redId = redIdGenerator.next(),
                    username = username,
                    passwordHash = passwordEncoder.encode(request.password),
                    displayName = displayName,
                    status = AccountStatus.PENDING
                )
            )
        } catch (_: DataIntegrityViolationException) {
            throw IllegalArgumentException("Username is already registered")
        }

        return AuthResponse(
            status = user.status,
            user = user.toResponse(),
            message = "ACCOUNT_PENDING_ADMIN_APPROVAL"
        )
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = users.findByUsernameIgnoreCase(request.username.trim())
            ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return when (user.status) {
            AccountStatus.APPROVED -> AuthResponse(
                status = user.status,
                user = user.toResponse(),
                accessToken = jwtService.issue(user),
                tokenType = "Bearer",
                expiresInSeconds = jwtService.expirationSeconds()
            )
            AccountStatus.PENDING -> AuthResponse(user.status, user.toResponse(), message = "ACCOUNT_PENDING_ADMIN_APPROVAL")
            AccountStatus.REJECTED -> AuthResponse(user.status, user.toResponse(), message = "ACCOUNT_REJECTED")
            AccountStatus.SUSPENDED -> AuthResponse(user.status, user.toResponse(), message = "ACCOUNT_SUSPENDED")
            AccountStatus.BANNED -> AuthResponse(user.status, user.toResponse(), message = "ACCOUNT_BANNED")
        }
    }
}

class InvalidCredentialsException : RuntimeException("Invalid username or password")
