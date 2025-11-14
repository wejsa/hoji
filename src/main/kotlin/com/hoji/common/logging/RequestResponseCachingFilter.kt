package com.hoji.common.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

/**
 * 요청/응답 Body를 캐싱하는 필터
 * 로깅을 위해 Request/Response Body를 여러 번 읽을 수 있도록 함
 */
@Component
class RequestResponseCachingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = if (request is ContentCachingRequestWrapper) {
            request
        } else {
            ContentCachingRequestWrapper(request)
        }

        val wrappedResponse = if (response is ContentCachingResponseWrapper) {
            response
        } else {
            ContentCachingResponseWrapper(response)
        }

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            // 응답 본문을 원래 응답으로 복사
            wrappedResponse.copyBodyToResponse()
        }
    }
}
