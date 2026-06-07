package com.jwtstarter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jwtstarter.common.context.RequestContextInterceptor
import com.jwtstarter.common.logging.LoggingInterceptor
import com.jwtstarter.common.metrics.MetricsInterceptor
import com.jwtstarter.config.WebMvcConfig
import com.jwtstarter.controller.dto.CreateUserRequest
import com.jwtstarter.controller.dto.UpdateUserRequest
import com.jwtstarter.controller.dto.UserResponse
import com.jwtstarter.domain.UserStatus
import com.jwtstarter.service.UserService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [UserController::class],
    // 보안 자동설정은 컨트롤러 로직 단위 테스트 범위 밖(인증/인가 e2e는 Step 5 통합 테스트에서 검증).
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    // @WebMvcTest는 WebMvcConfigurer와 모든 HandlerInterceptor를 슬라이스에 자동 포함한다.
    // 커스텀 인터셉터들은 LoggingProperties/CustomMetrics 등 비-웹 빈에 의존하므로
    // 컨트롤러 단위 테스트에서는 제외한다.
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
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userService: UserService

    @Test
    fun `should create user successfully`() {
        // given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            name = "Test User"
        )
        val response = UserResponse(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            name = "Test User",
            status = UserStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userService.createUser(any()) } returns response

        // when & then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))

        verify(exactly = 1) { userService.createUser(any()) }
    }

    @Test
    fun `should get user successfully`() {
        // given
        val userId = 1L
        val response = UserResponse(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            name = "Test User",
            status = UserStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userService.getUser(userId) } returns response

        // when & then
        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(userId))
            .andExpect(jsonPath("$.data.username").value("testuser"))

        verify(exactly = 1) { userService.getUser(userId) }
    }

    @Test
    fun `should get all users successfully`() {
        // given
        val users = listOf(
            UserResponse(
                id = 1L,
                username = "user1",
                email = "user1@example.com",
                name = "User 1",
                status = UserStatus.ACTIVE,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            UserResponse(
                id = 2L,
                username = "user2",
                email = "user2@example.com",
                name = "User 2",
                status = UserStatus.ACTIVE,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        every { userService.getAllUsers() } returns users

        // when & then
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))

        verify(exactly = 1) { userService.getAllUsers() }
    }

    @Test
    fun `should update user successfully`() {
        // given
        val userId = 1L
        // status 변경은 ADMIN 권한이 필요(보안 컨텍스트 의존)하므로 보안 제외 슬라이스에서는 email/name만 검증.
        // status 기반 접근 제어는 AuthIntegrationTest(e2e)에서 검증한다.
        val request = UpdateUserRequest(
            email = "newemail@example.com",
            name = "New Name",
            status = null
        )
        val response = UserResponse(
            id = userId,
            username = "testuser",
            email = "newemail@example.com",
            name = "New Name",
            status = UserStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userService.updateUser(userId, any()) } returns response

        // when & then
        mockMvc.perform(
            put("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("newemail@example.com"))
            .andExpect(jsonPath("$.data.name").value("New Name"))

        verify(exactly = 1) { userService.updateUser(userId, any()) }
    }

    @Test
    fun `should delete user successfully`() {
        // given
        val userId = 1L
        every { userService.deleteUser(userId) } returns Unit

        // when & then
        mockMvc.perform(delete("/api/v1/users/$userId"))
            .andExpect(status().isNoContent)

        verify(exactly = 1) { userService.deleteUser(userId) }
    }
}
