package com.jwtstarter.common.health

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 커스텀 Health Indicator
 */
@Component("custom")
class CustomHealthIndicator : HealthIndicator {

    override fun health(): Health {
        return try {
            // 여기에 실제 헬스 체크 로직을 추가할 수 있습니다
            // 예: 외부 API 체크, 파일 시스템 체크 등

            val checkResult = performHealthCheck()

            if (checkResult) {
                Health.up()
                    .withDetail("application", "spring-boot-jwt-starter")
                    .withDetail("status", "healthy")
                    .withDetail("timestamp", LocalDateTime.now().toString())
                    .build()
            } else {
                Health.down()
                    .withDetail("application", "spring-boot-jwt-starter")
                    .withDetail("status", "unhealthy")
                    .withDetail("timestamp", LocalDateTime.now().toString())
                    .build()
            }
        } catch (e: Exception) {
            logger.error(e) { "Health check failed" }
            Health.down()
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }

    private fun performHealthCheck(): Boolean {
        // 실제 헬스 체크 로직
        // 현재는 항상 true를 반환하지만, 실제로는 다양한 체크를 수행할 수 있습니다
        return true
    }
}
