package com.hoji.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress

/**
 * 메트릭 설정
 * 멀티 인스턴스 환경에서 각 인스턴스를 식별하기 위한 공통 태그 추가
 */
@Configuration
class MetricsConfig {

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    @Value("\${server.port}")
    private var serverPort: Int = 8080

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config().commonTags(
                listOf(
                    Tag.of("application", applicationName),
                    Tag.of("instance", getInstanceId()),
                    Tag.of("host", getHostName()),
                    Tag.of("port", serverPort.toString())
                )
            )
        }
    }

    /**
     * 인스턴스 ID 생성
     * 환경 변수로 받거나, 없으면 호스트명:포트로 생성
     */
    private fun getInstanceId(): String {
        // 1. 환경 변수에서 인스턴스 ID 가져오기 (Kubernetes Pod Name 등)
        val instanceIdFromEnv = System.getenv("INSTANCE_ID")
        if (!instanceIdFromEnv.isNullOrBlank()) {
            return instanceIdFromEnv
        }

        // 2. Kubernetes Pod Name
        val podName = System.getenv("HOSTNAME")
        if (!podName.isNullOrBlank() && podName.contains("-")) {
            return podName
        }

        // 3. 기본값: 호스트명:포트
        return "${getHostName()}:${serverPort}"
    }

    /**
     * 호스트명 가져오기
     */
    private fun getHostName(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "unknown"
        }
    }
}
