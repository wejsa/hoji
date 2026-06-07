package com.hoji.config

import com.hoji.config.properties.CorsProperties
import com.hoji.config.properties.JwtProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class PropertiesProfileValidatorTest {

    private val strongSecret = "this-is-a-strong-jwt-secret-key-with-32+bytes!!"
    private val defaultSecret = "hoji-local-development-secret-please-change-me!!"

    @Test
    fun `spring profiles로 prod 활성 시 와일드카드 CORS를 차단한다`() {
        val validator = PropertiesProfileValidator(
            MockEnvironment().apply { setActiveProfiles("prod") },
            CorsProperties(allowedOrigins = listOf("*"), allowCredentials = false),
            JwtProperties(secret = strongSecret),
        )

        assertThatThrownBy { validator.afterPropertiesSet() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Production CORS")
    }

    @Test
    fun `prod 활성 시 기본 JWT 시크릿을 차단한다`() {
        val validator = PropertiesProfileValidator(
            MockEnvironment().apply { setActiveProfiles("prod") },
            CorsProperties(allowedOrigins = listOf("https://app.example.com")),
            JwtProperties(secret = defaultSecret),
        )

        assertThatThrownBy { validator.afterPropertiesSet() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Default jwt.secret")
    }

    @Test
    fun `비-prod 프로파일에서는 로컬 기본값이 통과한다`() {
        val validator = PropertiesProfileValidator(
            MockEnvironment().apply { setActiveProfiles("dev") },
            CorsProperties(),
            JwtProperties(secret = defaultSecret),
        )

        assertThatCode { validator.afterPropertiesSet() }.doesNotThrowAnyException()
    }

    @Test
    fun `프로파일 미설정(무프로파일)에서는 통과한다`() {
        val validator = PropertiesProfileValidator(
            MockEnvironment(),
            CorsProperties(),
            JwtProperties(secret = defaultSecret),
        )

        assertThatCode { validator.afterPropertiesSet() }.doesNotThrowAnyException()
    }

    @Test
    fun `prod 판정은 부분 일치와 대소문자를 무시한다`() {
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("prod-eu"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("PROD"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("dev", "local"))).isFalse()
        assertThat(PropertiesProfileValidator.isProdProfile(emptyArray())).isFalse()
    }
}
