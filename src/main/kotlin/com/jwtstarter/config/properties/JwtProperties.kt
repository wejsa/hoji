package com.jwtstarter.config.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 설정 프로퍼티 (application.yml의 `jwt.*`).
 *
 * 시작 시점에 시크릿 강도와 운영 환경의 기본 시크릿 사용을 검증해
 * 잘못된 설정으로 토큰이 위조되는 것을 fail-fast로 차단한다.
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "",
    var accessTokenValidityMs: Long = 1_800_000,        // 30분
    var refreshTokenValidityMs: Long = 1_209_600_000    // 14일
) {
    /** 환경(프로파일) 무관 불변식. 바인딩 직후 fail-fast. */
    @PostConstruct
    fun validate() {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) {
            "jwt.secret must be at least 256 bits ($MIN_SECRET_BYTES bytes) for HS256"
        }
    }

    /**
     * prod 프로파일 의존 검증. prod에서 기본 시크릿 사용을 차단한다.
     *
     * prod 판정은 [com.jwtstarter.config.PropertiesProfileValidator]가 Spring [org.springframework.core.env.Environment]
     * 기반으로 산출해 주입한다(과거 `System.getenv`만 보던 우회 경로 차단). `isProd`를 순수 입력으로 받아 단위 테스트 가능하다.
     */
    fun validateForProfile(isProd: Boolean) {
        require(!(isProd && secret.startsWith(DEFAULT_SECRET_PREFIX))) {
            "Default jwt.secret detected in production. Inject a strong JWT_SECRET env var."
        }
    }

    companion object {
        private const val MIN_SECRET_BYTES = 32
        private const val DEFAULT_SECRET_PREFIX = "jwt-starter-local-development"
    }
}
