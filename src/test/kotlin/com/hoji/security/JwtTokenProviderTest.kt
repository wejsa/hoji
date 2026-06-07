package com.hoji.security

import com.hoji.config.properties.JwtProperties
import com.hoji.domain.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private val secret = "hoji-test-secret-key-which-is-long-enough-256-bits-0123456789"

    private fun provider(
        accessMs: Long = 1_800_000,
        refreshMs: Long = 1_209_600_000
    ) = JwtTokenProvider(
        JwtProperties(
            secret = secret,
            accessTokenValidityMs = accessMs,
            refreshTokenValidityMs = refreshMs
        )
    )

    @Test
    fun `access token은 발급 후 검증과 클레임 추출이 가능하다`() {
        val sut = provider()
        val token = sut.createAccessToken(userId = 7L, username = "alice", role = Role.ADMIN)

        assertThat(sut.validateToken(token)).isTrue()
        assertThat(sut.getUserId(token)).isEqualTo(7L)
        assertThat(sut.getUsername(token)).isEqualTo("alice")
    }

    @Test
    fun `토큰에서 생성한 Authentication은 ROLE_ 접두사 권한을 가진다`() {
        val sut = provider()
        val token = sut.createAccessToken(userId = 1L, username = "bob", role = Role.USER)

        val authentication = sut.getAuthentication(token)

        assertThat(authentication.name).isEqualTo("bob")
        assertThat(authentication.authorities.map { it.authority }).containsExactly("ROLE_USER")
    }

    @Test
    fun `만료된 토큰은 검증에 실패한다`() {
        val sut = provider(accessMs = -1_000) // 이미 만료
        val token = sut.createAccessToken(userId = 1L, username = "carol", role = Role.USER)

        assertThat(sut.validateToken(token)).isFalse()
    }

    @Test
    fun `위조되거나 형식이 잘못된 토큰은 검증에 실패한다`() {
        val sut = provider()

        assertThat(sut.validateToken("not-a-jwt")).isFalse()
        assertThat(sut.validateToken("")).isFalse()

        val token = sut.createAccessToken(1L, "dave", Role.USER)
        val tampered = token.dropLast(2) + "xx"
        assertThat(sut.validateToken(tampered)).isFalse()
    }
}
