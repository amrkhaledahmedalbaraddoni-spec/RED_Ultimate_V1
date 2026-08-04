package com.red.server.auth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException::class)
    fun invalidCredentials(): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "INVALID_CREDENTIALS"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(error: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (error.message ?: "INVALID_REQUEST")))

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(error: NoSuchElementException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (error.message ?: "NOT_FOUND")))
}
