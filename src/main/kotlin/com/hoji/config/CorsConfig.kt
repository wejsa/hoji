package com.hoji.config

import com.hoji.config.properties.CorsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * CORS 설정.
 *
 * 허용 오리진은 [CorsProperties] (`hoji.cors.*`)로 외부화한다.
 * 와일드카드(`*`) + 자격증명 동시 허용은 [CorsProperties.validate]가 시작 시 차단한다.
 */
@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties,
) {

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()

        // 허용 오리진 — 설정값(환경별)으로 외부화. 와일드카드 하드코딩 제거.
        config.allowedOrigins = corsProperties.allowedOrigins
        config.allowedMethods = corsProperties.allowedMethods
        config.allowedHeaders = corsProperties.allowedHeaders
        config.exposedHeaders = corsProperties.exposedHeaders
        config.allowCredentials = corsProperties.allowCredentials
        config.maxAge = corsProperties.maxAgeSeconds

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}
