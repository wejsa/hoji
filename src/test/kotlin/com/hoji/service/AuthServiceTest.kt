package com.hoji.service

import com.hoji.common.exception.ConflictException
import com.hoji.common.exception.UnauthorizedException
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.repository.UserRepository
import com.hoji.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<org.springframework.security.crypto.password.PasswordEncoder>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val authenticationManager = mockk<AuthenticationManager>()
    private val authService = AuthService(userRepository, passwordEncoder, jwtTokenProvider, authenticationManager)

    private fun signupRequest() = SignupRequest(
        username = "alice",
        email = "alice@example.com",
        password = "password123",
        name = "Alice"
    )

    @Test
    fun `signup은 중복이 없으면 USER 권한으로 생성하고 비밀번호를 해싱한다`() {
        every { userRepository.existsByUsername("alice") } returns false
        every { userRepository.existsByEmail("alice@example.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed-secret"
        val saved = slot<User>()
        every { userRepository.save(capture(saved)) } answers {
            User(
                id = 1L,
                username = saved.captured.username,
                email = saved.captured.email,
                password = saved.captured.password,
                name = saved.captured.name,
                role = saved.captured.role
            )
        }

        val result = authService.signup(signupRequest())

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.username).isEqualTo("alice")
        assertThat(result.role).isEqualTo(Role.USER)
        // 평문이 아닌 해시로 저장
        assertThat(saved.captured.password).isEqualTo("hashed-secret")
        assertThat(saved.captured.password).isNotEqualTo("password123")
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `signup은 username 중복 시 ConflictException`() {
        every { userRepository.existsByUsername("alice") } returns true

        assertThatThrownBy { authService.signup(signupRequest()) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Username already exists")
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `signup은 email 중복 시 ConflictException`() {
        every { userRepository.existsByUsername("alice") } returns false
        every { userRepository.existsByEmail("alice@example.com") } returns true

        assertThatThrownBy { authService.signup(signupRequest()) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("Email already exists")
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `login 성공 시 access·refresh 토큰을 발급한다`() {
        every { authenticationManager.authenticate(any()) } returns
            UsernamePasswordAuthenticationToken("alice", "password123") as Authentication
        val user = User(id = 7L, username = "alice", email = "alice@example.com", password = "hashed", name = "Alice", role = Role.USER)
        every { userRepository.findByUsername("alice") } returns user
        every { jwtTokenProvider.createAccessToken(7L, "alice", Role.USER) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(7L, "alice", Role.USER) } returns "refresh-token"

        val result = authService.login(LoginRequest("alice", "password123"))

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `login은 자격증명 불일치 시 UnauthorizedException으로 변환한다`() {
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("bad credentials")

        assertThatThrownBy { authService.login(LoginRequest("alice", "wrong")) }
            .isInstanceOf(UnauthorizedException::class.java)
            .hasMessageContaining("Invalid username or password")
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any(), any()) }
    }
}
