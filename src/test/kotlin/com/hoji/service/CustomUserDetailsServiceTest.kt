package com.hoji.service

import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.domain.UserStatus
import com.hoji.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val service = CustomUserDetailsService(userRepository)

    private fun user(role: Role = Role.USER, status: UserStatus = UserStatus.ACTIVE) =
        User(id = 1L, username = "alice", email = "alice@example.com", password = "hashed", name = "Alice", role = role, status = status)

    @Test
    fun `존재하는 사용자는 ROLE_ 접두 권한과 해시 비밀번호로 매핑된다`() {
        every { userRepository.findByUsername("alice") } returns user()

        val details = service.loadUserByUsername("alice")

        assertThat(details.username).isEqualTo("alice")
        assertThat(details.password).isEqualTo("hashed")
        assertThat(details.authorities.map { it.authority }).containsExactly("ROLE_USER")
        assertThat(details.isEnabled).isTrue()
    }

    @Test
    fun `ADMIN 사용자는 ROLE_ADMIN 권한을 가진다`() {
        every { userRepository.findByUsername("admin") } returns
            User(id = 2L, username = "admin", email = "admin@example.com", password = "h", name = "Admin", role = Role.ADMIN)

        val details = service.loadUserByUsername("admin")

        assertThat(details.authorities.map { it.authority }).containsExactly("ROLE_ADMIN")
    }

    @Test
    fun `없는 사용자는 UsernameNotFoundException`() {
        every { userRepository.findByUsername("ghost") } returns null

        assertThatThrownBy { service.loadUserByUsername("ghost") }
            .isInstanceOf(UsernameNotFoundException::class.java)
    }

    @Test
    fun `ACTIVE가 아닌 계정은 disabled 처리된다`() {
        every { userRepository.findByUsername("alice") } returns user(status = UserStatus.INACTIVE)

        val details = service.loadUserByUsername("alice")

        assertThat(details.isEnabled).isFalse()
    }
}
