package com.jwtstarter.config

import com.jwtstarter.config.properties.CorsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * CORS 설정.
 *
 * 허용 오리진은 [CorsProperties] (`jwtstarter.cors.*`)로 외부화한다.
 * 와일드카드(`*`) + 자격증명 동시 허용은 [CorsProperties.validate]가 시작 시 차단한다.
 */
@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties,
) {

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration())
        return CorsFilter(source)
    }

    /**
     * 설정값([CorsProperties])을 [CorsConfiguration]에 매핑한다.
     * 매핑 정확성을 단위 테스트로 검증할 수 있도록 빈 생성과 분리했다.
     */
    fun corsConfiguration(): CorsConfiguration = CorsConfiguration().apply {
        // 허용 오리진 — 설정값(환경별)으로 외부화. 와일드카드 하드코딩 제거.
        allowedOrigins = corsProperties.allowedOrigins
        allowedMethods = corsProperties.allowedMethods
        allowedHeaders = corsProperties.allowedHeaders
        exposedHeaders = corsProperties.exposedHeaders
        allowCredentials = corsProperties.allowCredentials
        maxAge = corsProperties.maxAgeSeconds
    }
}
