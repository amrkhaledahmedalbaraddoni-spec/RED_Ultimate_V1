package com.red.server.auth.security

import com.red.server.auth.model.AccountStatus
import com.red.server.auth.repository.UserAccountRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val users: UserAccountRepository
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')

        if (!token.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null) {
            runCatching {
                val user = users.findById(jwtService.userId(token)).orElse(null)
                if (user != null && user.status == AccountStatus.APPROVED) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(user.id.toString(), token, authorities)
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
