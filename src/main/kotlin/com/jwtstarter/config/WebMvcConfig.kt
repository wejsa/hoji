package com.jwtstarter.config

import com.jwtstarter.common.context.RequestContextInterceptor
import com.jwtstarter.common.logging.LoggingInterceptor
import com.jwtstarter.common.metrics.MetricsInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 설정
 */
@Configuration
class WebMvcConfig(
    private val requestContextInterceptor: RequestContextInterceptor,
    private val loggingInterceptor: LoggingInterceptor,
    private val metricsInterceptor: MetricsInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // Request Context 인터셉터 (가장 먼저 실행)
        registry.addInterceptor(requestContextInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")

        // 로깅 인터셉터
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")

        // 메트릭 수집 인터셉터
        registry.addInterceptor(metricsInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")
    }
}
