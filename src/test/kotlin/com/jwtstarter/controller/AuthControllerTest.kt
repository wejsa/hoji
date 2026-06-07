package com.jwtstarter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jwtstarter.common.context.RequestContextInterceptor
import com.jwtstarter.common.exception.UnauthorizedException
import com.jwtstarter.common.logging.LoggingInterceptor
import com.jwtstarter.common.metrics.MetricsInterceptor
import com.jwtstarter.config.WebMvcConfig
import com.jwtstarter.controller.dto.LoginRequest
import com.jwtstarter.controller.dto.SignupRequest
import com.jwtstarter.controller.dto.SignupResponse
import com.jwtstarter.controller.dto.TokenResponse
import com.jwtstarter.domain.Role
import com.jwtstarter.service.AuthService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [AuthController::class],
    // 보안 자동설정은 컨트롤러 로직 단위 테스트 범위 밖(인증/인가 e2e는 Step 5 통합 테스트에서 검증).
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                WebMvcConfig::class,
                RequestContextInterceptor::class,
                LoggingInterceptor::class,
                MetricsInterceptor::class
            ]
        )
    ]
)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `회원가입은 201과 사용자 정보를 반환한다`() {
        val request = SignupRequest("alice", "alice@example.com", "password123", "Alice")
        every { authService.signup(any()) } returns
            SignupResponse(id = 1L, username = "alice", email = "alice@example.com", name = "Alice", role = Role.USER)

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.role").value("USER"))

        verify(exactly = 1) { authService.signup(any()) }
    }

    @Test
    fun `로그인은 200과 토큰을 반환한다`() {
        val request = LoginRequest("alice", "password123")
        every { authService.login(any()) } returns TokenResponse("access-token", "refresh-token")

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
    }

    @Test
    fun `회원가입 입력 검증 실패 시 400`() {
        val invalid = SignupRequest("", "not-an-email", "short", "")

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))

        verify(exactly = 0) { authService.signup(any()) }
    }

    @Test
    fun `로그인 실패는 401로 매핑된다`() {
        every { authService.login(any()) } throws UnauthorizedException("Invalid username or password")

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("alice", "wrong")))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }
}
