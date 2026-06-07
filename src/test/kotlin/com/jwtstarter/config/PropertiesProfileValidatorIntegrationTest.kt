package com.jwtstarter.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * H001 회귀 앵커 (TASK-007).
 *
 * 단위 테스트([PropertiesProfileValidatorTest])는 `MockEnvironment.setActiveProfiles()`를 직접
 * 호출하므로, "`spring.profiles.active` 프로퍼티가 실제 Spring 환경 처리에서
 * [Environment.getActiveProfiles]에 반영된다"는 [PropertiesProfileValidator]의 핵심 가정을
 * 검증하지 못한다. 본 테스트는 프로퍼티 기반 프로파일 활성화 → Environment 반영 → prod 판정의
 * 배선을 실제 Spring 컨텍스트로 고정한다.
 *
 * 전체 [com.jwtstarter.JwtStarterApplication] 부팅은 `application-prod.yml`의 `ddl-auto: validate`(빈 H2에
 * 스키마 없음) 등으로 prod 프로파일에서 기동이 깨지므로, 프로파일 활성화 경로만 보는 최소 컨텍스트를 쓴다.
 */
@SpringBootTest(
    classes = [PropertiesProfileValidatorIntegrationTest.MinimalContext::class],
    properties = ["spring.profiles.active=prod"],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class PropertiesProfileValidatorIntegrationTest {

    @Configuration
    open class MinimalContext

    @Autowired
    private lateinit var environment: Environment

    @Test
    fun `spring_profiles_active 프로퍼티가 Environment activeProfiles에 반영되어 prod로 판정된다`() {
        assertThat(environment.activeProfiles).contains("prod")
        assertThat(PropertiesProfileValidator.isProdProfile(environment.activeProfiles)).isTrue()
    }
}
