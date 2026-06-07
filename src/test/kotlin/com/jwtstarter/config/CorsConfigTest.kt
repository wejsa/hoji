package com.jwtstarter.config

import com.jwtstarter.config.properties.CorsProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CorsConfigTest {

    @Test
    fun `allowCredentials=true와 와일드카드 오리진은 시작 시 거부된다`() {
        val props = CorsProperties(allowedOrigins = listOf("*"), allowCredentials = true)

        assertThatThrownBy { props.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("allowCredentials")
    }

    @Test
    fun `명시적 오리진 목록은 검증을 통과한다`() {
        val props = CorsProperties(
            allowedOrigins = listOf("http://localhost:3000", "https://app.example.com"),
            allowCredentials = true,
        )

        assertThatCode { props.validate() }.doesNotThrowAnyException()
    }

    @Test
    fun `credentials를 끄면 와일드카드 오리진도 검증을 통과한다`() {
        val props = CorsProperties(allowedOrigins = listOf("*"), allowCredentials = false)

        assertThatCode { props.validate() }.doesNotThrowAnyException()
    }

    @Test
    fun `공백이 포함된 와일드카드 오리진도 credentials와 함께 거부된다`() {
        val props = CorsProperties(allowedOrigins = listOf(" * "), allowCredentials = true)

        assertThatThrownBy { props.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("allowCredentials")
    }

    @Test
    fun `목록에 와일드카드가 섞여 있으면 credentials와 함께 거부된다`() {
        val props = CorsProperties(
            allowedOrigins = listOf("https://app.example.com", "*"),
            allowCredentials = true,
        )

        assertThatThrownBy { props.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `corsConfiguration은 프로퍼티 값을 그대로 매핑한다`() {
        val origins = listOf("http://localhost:3000", "https://app.example.com")
        val props = CorsProperties(
            allowedOrigins = origins,
            allowCredentials = true,
            maxAgeSeconds = 1234,
        )

        val config = CorsConfig(props).corsConfiguration()

        assertThat(config.allowedOrigins).isEqualTo(origins)
        assertThat(config.allowedOrigins).doesNotContain("*")
        assertThat(config.allowCredentials).isTrue()
        assertThat(config.maxAge).isEqualTo(1234L)
        assertThat(config.allowedMethods).contains("GET", "POST", "OPTIONS")
    }

    @Test
    fun `corsFilter 빈이 정상 생성된다`() {
        val props = CorsProperties(allowedOrigins = listOf("http://localhost:3000"))

        assertThat(CorsConfig(props).corsFilter()).isNotNull
    }
}
