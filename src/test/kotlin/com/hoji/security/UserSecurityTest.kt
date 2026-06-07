package com.hoji.security

import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication

/**
 * UserSecurity.isSelf 분기 검증 — @PreAuthorize SpEL 소유권 판정의 fail-closed 보장.
 */
class UserSecurityTest {

    private val userRepository = mockk<UserRepository>()
    private val userSecurity = UserSecurity(userRepository)

    private fun user(id: Long, username: String) =
        User(id = id, username = username, email = "$username@example.com", password = "h", name = username, role = Role.USER)

    @Test
    fun `미인증(authentication null)이면 false`() {
        assertThat(userSecurity.isSelf(1L, null)).isFalse()
    }

    @Test
    fun `인증 주체에 해당하는 사용자가 없으면 false`() {
        val auth = mockk<Authentication> { every { name } returns "ghost" }
        every { userRepository.findByUsername("ghost") } returns null

        assertThat(userSecurity.isSelf(1L, auth)).isFalse()
    }

    @Test
    fun `대상 userId가 인증 주체 본인과 다르면 false`() {
        val auth = mockk<Authentication> { every { name } returns "alice" }
        every { userRepository.findByUsername("alice") } returns user(7L, "alice")

        assertThat(userSecurity.isSelf(99L, auth)).isFalse()
    }

    @Test
    fun `대상 userId가 인증 주체 본인이면 true`() {
        val auth = mockk<Authentication> { every { name } returns "alice" }
        every { userRepository.findByUsername("alice") } returns user(7L, "alice")

        assertThat(userSecurity.isSelf(7L, auth)).isTrue()
    }
}
