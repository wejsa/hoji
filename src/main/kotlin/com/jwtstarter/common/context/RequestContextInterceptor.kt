package com.jwtstarter.common.context

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

/**
 * 요청 컨텍스트를 설정하는 인터셉터
 */
@Component
class RequestContextInterceptor : HandlerInterceptor {

    companion object {
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val HEADER_USER_ID = "X-User-ID"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Request ID 생성 또는 헤더에서 가져오기
        val requestId = request.getHeader(HEADER_REQUEST_ID) ?: UUID.randomUUID().toString()

        // User ID 헤더에서 가져오기
        val userId = request.getHeader(HEADER_USER_ID)

        // 기타 정보
        val userAgent = request.getHeader("User-Agent")
        val remoteAddr = getClientIp(request)

        // 주요 헤더들 수집
        val headers = mutableMapOf<String, String>()
        request.headerNames?.asIterator()?.forEach { headerName ->
            headers[headerName] = request.getHeader(headerName)
        }

        // 컨텍스트 설정
        val context = RequestContext(
            requestId = requestId,
            userId = userId,
            userAgent = userAgent,
            remoteAddr = remoteAddr,
            headers = headers
        )
        RequestContext.set(context)

        // 응답 헤더에 Request ID 추가
        response.setHeader(HEADER_REQUEST_ID, requestId)

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // 컨텍스트 정리
        RequestContext.clear()
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )

        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrEmpty() && !"unknown".equals(ip, ignoreCase = true)) {
                // X-Forwarded-For는 콤마로 구분된 여러 IP를 포함할 수 있음
                return ip.split(",")[0].trim()
            }
        }

        return request.remoteAddr ?: "unknown"
    }
}
