package com.hoji.config

import com.hoji.config.properties.CorsProperties
import com.hoji.config.properties.JwtProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * prod 프로파일 의존 설정을 시작 시점에 검증한다.
 *
 * [CorsProperties]/[JwtProperties]의 prod 가드는 과거 `System.getenv("SPRING_PROFILES_ACTIVE")`에
 * 의존해, `spring.profiles.active` 프로퍼티/`-D`/yml로 prod를 활성화하면 우회되었다(테스트도 불가).
 * Spring이 모든 활성화 경로를 반영한 [Environment.getActiveProfiles]를 기준으로 판정해
 * prod 와일드카드 CORS·기본 JWT 시크릿을 일관되게 fail-fast로 차단한다.
 */
@Component
class PropertiesProfileValidator(
    private val environment: Environment,
    private val corsProperties: CorsProperties,
    private val jwtProperties: JwtProperties,
) : InitializingBean {

    override fun afterPropertiesSet() {
        val isProd = isProdProfile(environment.activeProfiles)
        corsProperties.validateForProfile(isProd)
        jwtProperties.validateForProfile(isProd)
    }

    companion object {
        /**
         * 활성 프로파일 중 정확히 `prod`이거나 `prod-`로 시작하는 것이 있으면 prod로 판정한다(대소문자 무관).
         * 예: `prod`, `prod-eu` → prod. `non-prod`/`production` 같은 부분일치는 prod가 아니다
         * (느슨한 `contains("prod")`의 오탐 차단 — TASK-007 M001).
         */
        fun isProdProfile(activeProfiles: Array<String>): Boolean =
            activeProfiles.any { it.equals("prod", ignoreCase = true) || it.startsWith("prod-", ignoreCase = true) }
    }
}
