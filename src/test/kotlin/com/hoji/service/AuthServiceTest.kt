package com.hoji.service

import com.hoji.common.exception.ConflictException
import com.hoji.common.exception.NotFoundException
import com.hoji.common.exception.UnauthorizedException
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.RefreshRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.domain.RefreshToken
import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.repository.RefreshTokenRepository
import com.hoji.repository.UserRepository
import com.hoji.security.JwtTokenProvider
import io.mockk.every
import io.mockk.justRun
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
import java.time.LocalDateTime

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val passwordEncoder = mockk<org.springframework.security.crypto.password.PasswordEncoder>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val authenticationManager = mockk<AuthenticationManager>()
    private val authService = AuthService(
        userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider, authenticationManager
    )

    private fun signupRequest() = SignupRequest(
        username = "alice",
        email = "alice@example.com",
        password = "password123",
        name = "Alice"
    )

    private fun alice() =
        User(id = 7L, username = "alice", email = "alice@example.com", password = "hashed", name = "Alice", role = Role.USER)

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
    fun `login 성공 시 토큰을 발급하고 refresh 해시를 저장하며 자격증명으로 인증한다`() {
        val authToken = slot<UsernamePasswordAuthenticationToken>()
        every { authenticationManager.authenticate(capture(authToken)) } returns
            UsernamePasswordAuthenticationToken("alice", "password123") as Authentication
        every { userRepository.findByUsername("alice") } returns alice()
        every { jwtTokenProvider.createAccessToken(7L, "alice", Role.USER) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(7L, "alice", Role.USER) } returns "refresh-token"
        every { jwtTokenProvider.getExpiration("refresh-token") } returns LocalDateTime.now().plusDays(14)
        val saved = slot<RefreshToken>()
        every { refreshTokenRepository.save(capture(saved)) } answers { firstArg() }

        val result = authService.login(LoginRequest("alice", "password123"))

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.tokenType).isEqualTo("Bearer")
        // H004: authenticate가 입력 username/password로 호출됨
        assertThat(authToken.captured.principal).isEqualTo("alice")
        assertThat(authToken.captured.credentials).isEqualTo("password123")
        // refresh 토큰은 평문이 아닌 해시로 저장됨
        assertThat(saved.captured.userId).isEqualTo(7L)
        assertThat(saved.captured.token).isNotEqualTo("refresh-token")
        assertThat(saved.captured.token).matches("[0-9a-f]{64}")
    }

    @Test
    fun `login은 자격증명 불일치 시 UnauthorizedException으로 변환한다`() {
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("bad credentials")

        assertThatThrownBy { authService.login(LoginRequest("alice", "wrong")) }
            .isInstanceOf(UnauthorizedException::class.java)
            .hasMessageContaining("Invalid username or password")
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any(), any()) }
    }

    @Test
    fun `login은 인증 성공 후 사용자 조회가 null이면 Unauthorized`() {
        // H002: authenticate 통과 후 findByUsername null 분기 (정합성 방어)
        every { authenticationManager.authenticate(any()) } returns
            UsernamePasswordAuthenticationToken("alice", "pw") as Authentication
        every { userRepository.findByUsername("alice") } returns null

        assertThatThrownBy { authService.login(LoginRequest("alice", "pw")) }
            .isInstanceOf(UnauthorizedException::class.java)
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `refresh는 유효한 refresh 토큰으로 회전 발급한다(기존 폐기 후 신규 저장)`() {
        val rt = "valid-refresh-token"
        every { jwtTokenProvider.isRefreshToken(rt) } returns true
        every { refreshTokenRepository.findByToken(any()) } returns
            RefreshToken(id = 1L, userId = 7L, token = "old-hash", expiresAt = LocalDateTime.now().plusDays(1))
        justRun { refreshTokenRepository.deleteByToken(any()) }
        every { jwtTokenProvider.getUsername(rt) } returns "alice"
        every { userRepository.findByUsername("alice") } returns alice()
        every { jwtTokenProvider.createAccessToken(7L, "alice", Role.USER) } returns "new-access"
        every { jwtTokenProvider.createRefreshToken(7L, "alice", Role.USER) } returns "new-refresh"
        every { jwtTokenProvider.getExpiration("new-refresh") } returns LocalDateTime.now().plusDays(14)
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val result = authService.refresh(RefreshRequest(rt))

        assertThat(result.accessToken).isEqualTo("new-access")
        assertThat(result.refreshToken).isEqualTo("new-refresh")
        verify(exactly = 1) { refreshTokenRepository.deleteByToken(any()) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `refresh는 refresh 타입이 아닌(위조·만료·access) 토큰이면 Unauthorized`() {
        every { jwtTokenProvider.isRefreshToken("not-a-refresh") } returns false

        assertThatThrownBy { authService.refresh(RefreshRequest("not-a-refresh")) }
            .isInstanceOf(UnauthorizedException::class.java)
        verify(exactly = 0) { refreshTokenRepository.deleteByToken(any()) }
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any(), any()) }
    }

    @Test
    fun `refresh는 폐기된(저장 안 된) 토큰이면 Unauthorized — 로그아웃·재사용 차단`() {
        val rt = "rotated-or-logged-out"
        every { jwtTokenProvider.isRefreshToken(rt) } returns true
        every { refreshTokenRepository.findByToken(any()) } returns null

        assertThatThrownBy { authService.refresh(RefreshRequest(rt)) }
            .isInstanceOf(UnauthorizedException::class.java)
        verify(exactly = 0) { jwtTokenProvider.createAccessToken(any(), any(), any()) }
    }

    @Test
    fun `logout은 보유 refresh 토큰을 해시로 폐기한다`() {
        val hash = slot<String>()
        justRun { refreshTokenRepository.deleteByToken(capture(hash)) }

        authService.logout(RefreshRequest("some-refresh-token"))

        verify(exactly = 1) { refreshTokenRepository.deleteByToken(any()) }
        assertThat(hash.captured).matches("[0-9a-f]{64}")
    }

    @Test
    fun `me는 현재 사용자 정보를 반환한다`() {
        every { userRepository.findByUsername("alice") } returns alice()

        val result = authService.me("alice")

        assertThat(result.id).isEqualTo(7L)
        assertThat(result.username).isEqualTo("alice")
        assertThat(result.email).isEqualTo("alice@example.com")
        assertThat(result.role).isEqualTo(Role.USER)
    }

    @Test
    fun `me는 존재하지 않는 사용자면 NotFound`() {
        every { userRepository.findByUsername("ghost") } returns null

        assertThatThrownBy { authService.me("ghost") }
            .isInstanceOf(NotFoundException::class.java)
    }
}
