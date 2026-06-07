package com.jwtstarter.common.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

/**
 * 커스텀 메트릭 수집기
 * 비즈니스 메트릭을 수집하고 Prometheus로 노출
 */
@Component
class CustomMetrics(
    private val meterRegistry: MeterRegistry
) {

    /**
     * API 호출 횟수 카운터
     */
    fun incrementApiCall(endpoint: String, method: String, status: Int) {
        Counter.builder("api.calls")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .tag("status", status.toString())
            .description("API 호출 횟수")
            .register(meterRegistry)
            .increment()
    }

    /**
     * 비즈니스 이벤트 카운터
     */
    fun incrementBusinessEvent(eventType: String, result: String = "success") {
        Counter.builder("business.events")
            .tag("type", eventType)
            .tag("result", result)
            .description("비즈니스 이벤트 발생 횟수")
            .register(meterRegistry)
            .increment()
    }

    /**
     * 에러 카운터
     */
    fun incrementError(errorType: String, errorCode: String) {
        Counter.builder("errors")
            .tag("type", errorType)
            .tag("code", errorCode)
            .description("에러 발생 횟수")
            .register(meterRegistry)
            .increment()
    }

    /**
     * 처리 시간 타이머
     */
    fun recordProcessingTime(operation: String, timeMillis: Long) {
        Timer.builder("processing.time")
            .tag("operation", operation)
            .description("작업 처리 시간")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(timeMillis))
    }

    /**
     * 특정 작업을 타이밍하는 헬퍼 메서드
     */
    fun <T> time(operation: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        return try {
            block()
        } finally {
            recordProcessingTime(operation, System.currentTimeMillis() - start)
        }
    }
}
