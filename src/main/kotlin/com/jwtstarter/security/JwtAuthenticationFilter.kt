package com.jwtstarter.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 매 요청마다 Authorization 헤더의 Bearer 토큰을 검증해 SecurityContext에 인증을 주입한다.
 */
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        resolveToken(request)?.let { token ->
            // Access 토큰만 인증으로 인정한다. Refresh 토큰을 Authorization 헤더로 보내도 인증되지 않는다.
            if (jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token)) {
                SecurityContextHolder.getContext().authentication =
                    jwtTokenProvider.getAuthentication(token)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (bearer.startsWith(BEARER_PREFIX)) bearer.substring(BEARER_PREFIX.length) else null
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
