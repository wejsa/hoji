package com.hoji.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 로깅 관련 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "hoji.logging")
data class LoggingProperties(
    /**
     * 민감정보 마스킹 대상 헤더 목록
     */
    var sensitiveHeaders: List<String> = listOf(
        "authorization",
        "cookie",
        "set-cookie",
        "x-api-key",
        "x-auth-token"
    )
)
