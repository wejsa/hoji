package com.hoji.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.function.Supplier

/**
 * RestTemplate 설정
 */
@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(10))
            .requestFactory(Supplier { BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory()) })
            .interceptors(loggingInterceptor())
            .build()
    }

    /**
     * 로깅 인터셉터
     */
    private fun loggingInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request, body, execution ->
            // 요청 로깅
            println(">>> RestTemplate Request: ${request.method} ${request.uri}")
            request.headers.forEach { name, values ->
                values.forEach { value ->
                    println(">>> Request Header: $name: $value")
                }
            }
            if (body.isNotEmpty()) {
                println(">>> Request Body: ${String(body)}")
            }

            // 요청 실행
            val response = execution.execute(request, body)

            // 응답 로깅
            println("<<< RestTemplate Response: ${response.statusCode}")
            response.headers.forEach { name, values ->
                values.forEach { value ->
                    println("<<< Response Header: $name: $value")
                }
            }

            response
        }
    }
}
