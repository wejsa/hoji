package com.hoji.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * CORS 설정
 */
@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()

        // 허용할 오리진 (환경별로 다르게 설정 필요)
        config.allowedOriginPatterns = listOf("*")  // Prod에서는 특정 도메인만 허용

        // 허용할 HTTP 메서드
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

        // 허용할 헤더
        config.allowedHeaders = listOf("*")

        // 노출할 헤더
        config.exposedHeaders = listOf(
            "X-Request-ID",
            "Authorization",
            "Content-Type",
            "Content-Disposition"
        )

        // 인증 정보 포함 허용
        config.allowCredentials = true

        // Preflight 요청 캐시 시간 (초)
        config.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}
