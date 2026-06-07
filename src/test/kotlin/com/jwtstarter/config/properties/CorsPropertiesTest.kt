package com.jwtstarter.config.properties

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CorsPropertiesTest {

    @Test
    fun `prod 프로파일에서 와일드카드 오리진은 거부된다`() {
        val props = CorsProperties(allowedOrigins = listOf("*"), allowCredentials = false)

        assertThatThrownBy { props.validateForProfile(isProd = true) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Production CORS")
    }

    @Test
    fun `공백 포함 와일드카드도 prod에서 거부된다`() {
        val props = CorsProperties(allowedOrigins = listOf(" * "), allowCredentials = false)

        assertThatThrownBy { props.validateForProfile(isProd = true) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `비-prod에서는 와일드카드 오리진이 허용된다`() {
        val props = CorsProperties(allowedOrigins = listOf("*"), allowCredentials = false)

        assertThatCode { props.validateForProfile(isProd = false) }.doesNotThrowAnyException()
    }

    @Test
    fun `prod라도 명시 오리진 목록은 통과한다`() {
        val props = CorsProperties(allowedOrigins = listOf("https://app.example.com"))

        assertThatCode { props.validateForProfile(isProd = true) }.doesNotThrowAnyException()
    }

    @Test
    fun `allowCredentials=true와 와일드카드는 프로파일 무관 거부된다`() {
        val props = CorsProperties(allowedOrigins = listOf("*"), allowCredentials = true)

        assertThatThrownBy { props.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("allowCredentials")
    }
}
