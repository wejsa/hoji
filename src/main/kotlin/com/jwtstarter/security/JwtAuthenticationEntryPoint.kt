package com.jwtstarter.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.jwtstarter.common.dto.ApiResponse
import com.jwtstarter.common.dto.ResultCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * 인증되지 않은 접근(401)을 공통 ApiResponse(UNAUTHORIZED) 포맷으로 반환한다.
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        objectMapper.writeValue(response.writer, ApiResponse.error<Unit>(ResultCode.UNAUTHORIZED))
    }
}
