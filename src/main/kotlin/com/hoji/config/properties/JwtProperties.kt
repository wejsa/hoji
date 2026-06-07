package com.hoji.config.properties

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
    @PostConstruct
    fun validate() {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) {
            "jwt.secret must be at least 256 bits ($MIN_SECRET_BYTES bytes) for HS256"
        }
        val isProd = System.getenv("SPRING_PROFILES_ACTIVE")?.contains("prod") == true
        require(!(isProd && secret.startsWith(DEFAULT_SECRET_PREFIX))) {
            "Default jwt.secret detected in production. Inject a strong JWT_SECRET env var."
        }
    }

    companion object {
        private const val MIN_SECRET_BYTES = 32
        private const val DEFAULT_SECRET_PREFIX = "hoji-local-development"
    }
}
