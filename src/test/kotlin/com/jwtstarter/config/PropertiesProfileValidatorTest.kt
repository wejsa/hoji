package com.jwtstarter.config

import com.jwtstarter.config.properties.CorsProperties
import com.jwtstarter.config.properties.JwtProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class PropertiesProfileValidatorTest {

    private val strongSecret = "this-is-a-strong-jwt-secret-key-with-32+bytes!!"
    private val defaultSecret = "jwt-starter-local-development-secret-please-change-me!!"

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
    fun `prod 판정은 prod·prod- 접두사와 대소문자를 무시한다`() {
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("prod-eu"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("PROD"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("dev", "local"))).isFalse()
        assertThat(PropertiesProfileValidator.isProdProfile(emptyArray())).isFalse()
    }

    @Test
    fun `다원소 프로파일 배열에 prod가 섞여 있으면 prod로 판정한다`() {
        // H002: any{} 가 다원소 배열을 끝까지 순회해 prod를 찾는지 고정
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("dev", "prod"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("monitoring", "prod-eu", "k8s"))).isTrue()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("dev", "local", "test"))).isFalse()
    }

    @Test
    fun `prod 부분일치(non-prod·production)는 prod가 아니다`() {
        // M001: 느슨한 contains("prod") 오탐 차단
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("non-prod"))).isFalse()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("production"))).isFalse()
        assertThat(PropertiesProfileValidator.isProdProfile(arrayOf("reproduction"))).isFalse()
    }

    @Test
    fun `다원소 프로파일에 prod가 섞이면 prod 가드가 발동한다`() {
        val validator = PropertiesProfileValidator(
            MockEnvironment().apply { setActiveProfiles("dev", "prod") },
            CorsProperties(allowedOrigins = listOf("*"), allowCredentials = false),
            JwtProperties(secret = strongSecret),
        )

        assertThatThrownBy { validator.afterPropertiesSet() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Production CORS")
    }
}
