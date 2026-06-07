package com.hoji.config.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * CORS 설정 프로퍼티 (application*.yml의 `hoji.cors.*`).
 *
 * 허용 오리진을 환경별 설정값으로 외부화한다. 기존의 와일드카드(`*`) + `allowCredentials=true`
 * 조합은 모든 출처에 자격증명 포함 요청을 허용하는 위험 설정이므로,
 * 시작 시점에 fail-fast로 차단한다 ([JwtProperties]의 prod 검증과 동일 패턴).
 *
 * 무프로파일 실행을 위해 기본값은 로컬 개발 오리진으로 둔다.
 */
@ConfigurationProperties(prefix = "hoji.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = listOf("http://localhost:3000"),
    var allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
    var allowedHeaders: List<String> = listOf("*"),
    var exposedHeaders: List<String> = listOf(
        "X-Request-ID",
        "Authorization",
        "Content-Type",
        "Content-Disposition",
    ),
    var allowCredentials: Boolean = true,
    var maxAgeSeconds: Long = 3600,
) {
    @PostConstruct
    fun validate() {
        require(!(allowCredentials && hasWildcardOrigin())) {
            "hoji.cors: allowCredentials=true와 allowedOrigins '$WILDCARD'는 동시 사용 불가. " +
                "허용 오리진을 명시하세요."
        }
        val isProd = System.getenv("SPRING_PROFILES_ACTIVE")?.contains("prod") == true
        require(!(isProd && hasWildcardOrigin())) {
            "Production CORS에 와일드카드 오리진('$WILDCARD')은 사용할 수 없습니다. 허용 도메인을 명시하세요."
        }
    }

    /** 공백을 무시하고 와일드카드('*') 오리진 포함 여부를 검사한다 (`" * "` 우회 차단). */
    private fun hasWildcardOrigin(): Boolean = allowedOrigins.any { it.trim() == WILDCARD }

    companion object {
        private const val WILDCARD = "*"
    }
}
