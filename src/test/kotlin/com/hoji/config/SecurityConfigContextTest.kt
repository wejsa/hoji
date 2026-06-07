package com.hoji.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles

/**
 * SecurityConfig + JWT 컴포넌트가 포함된 전체 애플리케이션 컨텍스트가 정상 부팅되는지 검증한다.
 * (스택 정리로 RabbitMQ/Redis 제거 후 앱이 기동 가능해졌음을 함께 보증)
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigContextTest {

    @Autowired
    private lateinit var securityFilterChain: SecurityFilterChain

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `보안 관련 빈이 정상 등록되고 컨텍스트가 부팅된다`() {
        assertThat(securityFilterChain).isNotNull()
        assertThat(authenticationManager).isNotNull()
    }

    @Test
    fun `passwordEncoder는 BCrypt로 동작한다`() {
        val encoded = passwordEncoder.encode("plain-password")
        assertThat(encoded).isNotEqualTo("plain-password")
        assertThat(passwordEncoder.matches("plain-password", encoded)).isTrue()
    }
}
