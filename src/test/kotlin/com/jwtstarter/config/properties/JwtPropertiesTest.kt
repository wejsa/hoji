package com.jwtstarter.config.properties

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class JwtPropertiesTest {

    private val strongSecret = "this-is-a-strong-jwt-secret-key-with-32+bytes!!"
    private val defaultSecret = "jwt-starter-local-development-secret-please-change-me!!"

    @Test
    fun `prod 프로파일에서 기본 시크릿은 거부된다`() {
        val props = JwtProperties(secret = defaultSecret)

        assertThatThrownBy { props.validateForProfile(isProd = true) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Default jwt.secret")
    }

    @Test
    fun `비-prod에서는 기본 시크릿이 허용된다`() {
        val props = JwtProperties(secret = defaultSecret)

        assertThatCode { props.validateForProfile(isProd = false) }.doesNotThrowAnyException()
    }

    @Test
    fun `prod에서 강한 시크릿은 통과한다`() {
        val props = JwtProperties(secret = strongSecret)

        assertThatCode { props.validateForProfile(isProd = true) }.doesNotThrowAnyException()
    }

    @Test
    fun `256비트 미만 시크릿은 프로파일 무관 거부된다`() {
        val props = JwtProperties(secret = "short")

        assertThatThrownBy { props.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("256 bits")
    }
}
