package com.red.server.auth

import com.red.server.auth.model.AccountRole
import com.red.server.auth.model.AccountStatus
import com.red.server.auth.model.DeviceStatus
import com.red.server.auth.model.UserAccount
import com.red.server.auth.model.UserDevice
import com.red.server.auth.repository.UserAccountRepository
import com.red.server.auth.repository.UserDeviceRepository
import com.red.server.auth.security.JwtService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegistrationService(
    private val users: UserAccountRepository,
    private val devices: UserDeviceRepository,
    private val passwordEncoder: PasswordEncoder,
    private val redIdGenerator: RedIdGenerator,
    private val enrollment: DeviceEnrollmentService,
    private val recovery: RecoveryService,
    private val refreshTokens: RefreshTokenService,
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
        val device = enrollment.enroll(user, request.device)
        val recoveryCodes = recovery.createFor(user)
        return AuthResponse(
            status = user.status,
            user = user.toResponse(listOf(device)),
            deviceId = device.id,
            recoveryCodes = recoveryCodes,
            message = "ACCOUNT_PENDING_ADMIN_APPROVAL"
        )
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = users.findByUsernameIgnoreCase(request.username.trim())
            ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) throw InvalidCredentialsException()
        val accountDevices = devices.findAllByUserIdOrderByCreatedAtAsc(user.id)

        if (user.status != AccountStatus.APPROVED) {
            return blockedResponse(user, accountDevices)
        }

        val device = resolveApprovedDevice(user, request.deviceId)
        val refresh = refreshTokens.issue(user, device)
        return AuthResponse(
            status = user.status,
            user = user.toResponse(accountDevices),
            deviceId = device?.id,
            accessToken = jwtService.issue(user, device?.id),
            refreshToken = refresh.token,
            tokenType = "Bearer",
            expiresInSeconds = jwtService.expirationSeconds()
        )
    }

    @Transactional
    fun refresh(request: RefreshRequest): RefreshResponse {
        val rotated = refreshTokens.rotate(request.refreshToken)
        val user = rotated.session.user
        require(user.status == AccountStatus.APPROVED) { "Account is not approved" }
        val device = rotated.session.device
        require(device == null || device.status == DeviceStatus.APPROVED) { "Device is not approved" }
        return RefreshResponse(
            accessToken = jwtService.issue(user, device?.id),
            refreshToken = rotated.token,
            expiresInSeconds = jwtService.expirationSeconds()
        )
    }

    fun logout(request: LogoutRequest) = refreshTokens.revoke(request.refreshToken)

    private fun resolveApprovedDevice(user: UserAccount, deviceId: java.util.UUID?): UserDevice? {
        if (user.role == AccountRole.ADMIN && deviceId == null) return null
        requireNotNull(deviceId) { "deviceId is required" }
        val device = devices.findByIdAndUserId(deviceId, user.id)
            ?: throw InvalidCredentialsException()
        require(device.status == DeviceStatus.APPROVED) { "Device is not approved" }
        return device
    }

    private fun blockedResponse(user: UserAccount, accountDevices: List<UserDevice>): AuthResponse {
        val message = when (user.status) {
            AccountStatus.PENDING -> "ACCOUNT_PENDING_ADMIN_APPROVAL"
            AccountStatus.REJECTED -> "ACCOUNT_REJECTED"
            AccountStatus.SUSPENDED -> "ACCOUNT_SUSPENDED"
            AccountStatus.BANNED -> "ACCOUNT_BANNED"
            AccountStatus.APPROVED -> error("Approved accounts are not blocked")
        }
        return AuthResponse(user.status, user.toResponse(accountDevices), message = message)
    }
}

class InvalidCredentialsException : RuntimeException("Invalid username or password")
