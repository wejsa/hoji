package com.hoji.common.logging

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

/**
 * 요청/응답 로깅 인터셉터
 */
@Component
class LoggingInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val startTime = System.currentTimeMillis()
        request.setAttribute("startTime", startTime)

        // 요청 정보 로깅
        logRequest(request)

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as? Long ?: return
        val duration = System.currentTimeMillis() - startTime

        // 응답 정보 로깅
        logResponse(request, response, duration, ex)
    }

    private fun logRequest(request: HttpServletRequest) {
        val uri = request.requestURI
        val method = request.method
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val headers = getHeaders(request)
        val body = if (request is ContentCachingRequestWrapper) {
            getRequestBody(request)
        } else {
            "[body not cached]"
        }

        logger.info {
            """
            |>>> REQUEST >>>
            |URI: $method $uri$queryString
            |Headers: $headers
            |Body: $body
            """.trimMargin()
        }
    }

    private fun logResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
        duration: Long,
        ex: Exception?
    ) {
        val uri = request.requestURI
        val method = request.method
        val status = response.status
        val body = if (response is ContentCachingResponseWrapper) {
            getResponseBody(response)
        } else {
            "[body not cached]"
        }

        val exceptionInfo = ex?.let { "\nException: ${it.message}" } ?: ""

        logger.info {
            """
            |<<< RESPONSE <<<
            |URI: $method $uri
            |Status: $status
            |Duration: ${duration}ms
            |Body: $body$exceptionInfo
            """.trimMargin()
        }
    }

    private fun getHeaders(request: HttpServletRequest): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        request.headerNames?.asIterator()?.forEach { headerName ->
            val headerValue = request.getHeader(headerName)
            // 민감한 정보는 마스킹
            headers[headerName] = if (isSensitiveHeader(headerName)) {
                "***"
            } else {
                headerValue
            }
        }
        return headers
    }

    private fun isSensitiveHeader(headerName: String): Boolean {
        val sensitiveHeaders = listOf("authorization", "cookie", "set-cookie", "x-api-key", "x-auth-token")
        return sensitiveHeaders.any { it.equals(headerName, ignoreCase = true) }
    }

    private fun getRequestBody(request: ContentCachingRequestWrapper): String {
        val content = request.contentAsByteArray
        return if (content.isNotEmpty()) {
            String(content, StandardCharsets.UTF_8)
        } else {
            "[empty]"
        }
    }

    private fun getResponseBody(response: ContentCachingResponseWrapper): String {
        val content = response.contentAsByteArray
        return if (content.isNotEmpty()) {
            String(content, StandardCharsets.UTF_8)
        } else {
            "[empty]"
        }
    }
}
