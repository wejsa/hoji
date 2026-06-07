package com.jwtstarter.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.jwtstarter.common.dto.ApiResponse
import com.jwtstarter.common.dto.ResultCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * 인증되었으나 권한이 부족한 접근(403)을 공통 ApiResponse(FORBIDDEN) 포맷으로 반환한다.
 */
@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        objectMapper.writeValue(response.writer, ApiResponse.error<Unit>(ResultCode.FORBIDDEN))
    }
}
