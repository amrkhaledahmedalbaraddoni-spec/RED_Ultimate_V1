package com.red.server.auth

import com.red.server.auth.model.AccountStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val registration: RegistrationService) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(registration.register(request))

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = registration.login(request)
        val status = when (response.status) {
            AccountStatus.APPROVED -> HttpStatus.OK
            AccountStatus.PENDING -> HttpStatus.LOCKED
            AccountStatus.REJECTED, AccountStatus.SUSPENDED, AccountStatus.BANNED -> HttpStatus.FORBIDDEN
        }
        return ResponseEntity.status(status).body(response)
    }
}
