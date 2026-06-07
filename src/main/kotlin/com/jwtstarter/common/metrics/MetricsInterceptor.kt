package com.jwtstarter.common.metrics

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 메트릭 수집 인터셉터
 */
@Component
class MetricsInterceptor(
    private val customMetrics: CustomMetrics
) : HandlerInterceptor {

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val endpoint = request.requestURI
        val method = request.method
        val status = response.status

        // API 호출 메트릭 수집
        customMetrics.incrementApiCall(endpoint, method, status)

        // 에러 메트릭 수집
        if (status >= 400) {
            customMetrics.incrementError(
                errorType = if (status >= 500) "server_error" else "client_error",
                errorCode = status.toString()
            )
        }
    }
}
