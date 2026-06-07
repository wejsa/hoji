package com.jwtstarter.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.LoggingCodecSupport
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * WebClient 설정
 */
@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        // HTTP Client 설정
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected { connection ->
                connection.addHandlerLast(ReadTimeoutHandler(10, TimeUnit.SECONDS))
                connection.addHandlerLast(WriteTimeoutHandler(10, TimeUnit.SECONDS))
            }

        // Exchange Strategies 설정 (메모리 버퍼 크기 조정)
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                // 로깅 활성화
                if (configurer.defaultCodecs() is LoggingCodecSupport) {
                    (configurer.defaultCodecs() as LoggingCodecSupport).isEnableLoggingRequestDetails = true
                }
            }
            .build()

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .filter(logRequest())
            .filter(logResponse())
            .build()
    }

    /**
     * 요청 로깅 필터
     */
    private fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            println(">>> WebClient Request: ${request.method()} ${request.url()}")
            request.headers().forEach { name, values ->
                values.forEach { value ->
                    println(">>> Request Header: $name: $value")
                }
            }
            Mono.just(request)
        }
    }

    /**
     * 응답 로깅 필터
     */
    private fun logResponse(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor { response ->
            println("<<< WebClient Response: ${response.statusCode()}")
            response.headers().asHttpHeaders().forEach { name, values ->
                values.forEach { value ->
                    println("<<< Response Header: $name: $value")
                }
            }
            Mono.just(response)
        }
    }
}
