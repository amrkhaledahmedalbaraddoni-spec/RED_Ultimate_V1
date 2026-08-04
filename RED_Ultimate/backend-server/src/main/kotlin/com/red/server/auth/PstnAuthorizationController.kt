package com.red.server.auth

import com.red.server.auth.repository.UserAccountRepository
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/admin/users/pstn")
class PstnAuthorizationController(private val users: UserAccountRepository) {
    @PutMapping
    @Transactional
    fun update(@RequestBody request: PstnAuthorizationRequest): UserAccountResponse {
        require(request.dailyLimit in 0..1000) { "dailyLimit must be between 0 and 1000" }
        val user = users.findById(request.userId).orElseThrow { NoSuchElementException("User not found") }
        user.pstnEnabled = request.enabled
        user.pstnDailyLimit = if (request.enabled) request.dailyLimit else 0
        user.updatedAt = Instant.now()
        return users.save(user).toResponse()
    }
}

data class PstnAuthorizationRequest(val userId: UUID, val enabled: Boolean, val dailyLimit: Int)
