package com.jwtstarter.config

import com.jwtstarter.common.context.RequestContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import java.util.*

/**
 * JPA Auditing 설정
 */
@Configuration
class AuditConfig {

    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAware {
            // RequestContext에서 사용자 ID 가져오기
            // 없으면 "system" 사용
            Optional.ofNullable(RequestContext.getUserId() ?: "system")
        }
    }
}
