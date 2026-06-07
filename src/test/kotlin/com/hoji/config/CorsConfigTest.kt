package com.hoji.config

import com.hoji.config.properties.CorsProperties
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
    fun `corsFilter는 설정된 오리진으로 정상 구성된다`() {
        val origins = listOf("http://localhost:3000", "https://app.example.com")
        val props = CorsProperties(allowedOrigins = origins, allowCredentials = true)

        val filter = CorsConfig(props).corsFilter()

        assertThat(filter).isNotNull
        assertThat(props.allowedOrigins).doesNotContain("*")
    }
}
