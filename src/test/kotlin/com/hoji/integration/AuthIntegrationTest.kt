package com.hoji.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.RefreshRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.controller.dto.TokenResponse
import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.domain.UserStatus
import com.hoji.repository.RefreshTokenRepository
import com.hoji.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 인증/인가 e2e 통합 테스트 (TASK-001 Step 5).
 *
 * 전체 컨텍스트(보안 필터 + 메서드 보안 + 실제 BCrypt + JWT)를 띄워 다음을 검증한다.
 * - 가입 → 로그인 → 보호 API 접근(200), 무토큰(401), refresh-as-access 차단(401)
 * - 실제 BCrypt 매칭(200) / 틀린 비밀번호(401) / INACTIVE·DELETED 계정(401)
 * - RBAC: ADMIN 전용 엔드포인트에 USER 접근(403), ADMIN 접근(200), 본인/타인 단건 조회(200/403)
 * - Refresh 회전: 재발급(200) 후 구 토큰 재사용 차단, 로그아웃 후 재사용 차단(401)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun clean() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun seedUser(
        username: String,
        rawPassword: String,
        role: Role = Role.USER,
        status: UserStatus = UserStatus.ACTIVE
    ): User = userRepository.save(
        User(
            username = username,
            email = "$username@example.com",
            password = passwordEncoder.encode(rawPassword),
            name = username,
            role = role,
            status = status
        )
    )

    private fun login(username: String, password: String): TokenResponse {
        val body = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest(username, password)))
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val data = objectMapper.readTree(body).get("data")
        return objectMapper.treeToValue(data, TokenResponse::class.java)
    }

    private fun bearer(token: String) = "Bearer $token"

    // ----- 가입 → 로그인 → 보호 API -----

    @Test
    fun `가입 후 로그인하면 발급된 access 토큰으로 보호 API(me)에 접근할 수 있다`() {
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SignupRequest("alice", "alice@example.com", "password123", "Alice")))
        ).andExpect(status().isCreated)

        val tokens = login("alice", "password123")

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokens.accessToken)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.role").value("USER"))
    }

    @Test
    fun `무토큰으로 보호 API 접근 시 401`() {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh 토큰을 Authorization으로 보내도 인증되지 않는다 — 401`() {
        seedUser("bob", "password123")
        val tokens = login("bob", "password123")

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokens.refreshToken)))
            .andExpect(status().isUnauthorized)
    }

    // ----- 실제 BCrypt / 계정 상태 -----

    @Test
    fun `올바른 비밀번호는 200, 틀린 비밀번호는 401`() {
        seedUser("carol", "correct-horse")

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("carol", "correct-horse")))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("carol", "wrong-password")))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `INACTIVE·DELETED 계정은 자격증명이 맞아도 로그인 401`() {
        seedUser("inactive", "password123", status = UserStatus.INACTIVE)
        seedUser("deleted", "password123", status = UserStatus.DELETED)

        for (username in listOf("inactive", "deleted")) {
            mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginRequest(username, "password123")))
            ).andExpect(status().isUnauthorized)
        }
    }

    // ----- RBAC -----

    @Test
    fun `USER는 ADMIN 전용 목록 조회에 접근 시 403, ADMIN은 200`() {
        seedUser("user1", "password123", role = Role.USER)
        seedUser("admin1", "password123", role = Role.ADMIN)

        val userToken = login("user1", "password123").accessToken
        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(userToken)))
            .andExpect(status().isForbidden)

        val adminToken = login("admin1", "password123").accessToken
        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `단건 조회는 본인은 200, 타인은 403, ADMIN은 타인도 200`() {
        val me = seedUser("owner", "password123", role = Role.USER)
        val other = seedUser("other", "password123", role = Role.USER)
        seedUser("admin2", "password123", role = Role.ADMIN)

        val ownerToken = login("owner", "password123").accessToken
        mockMvc.perform(get("/api/v1/users/${me.id}").header("Authorization", bearer(ownerToken)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.username").value("owner"))

        mockMvc.perform(get("/api/v1/users/${other.id}").header("Authorization", bearer(ownerToken)))
            .andExpect(status().isForbidden)

        val adminToken = login("admin2", "password123").accessToken
        mockMvc.perform(get("/api/v1/users/${other.id}").header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk)
    }

    // ----- Refresh 회전 / 로그아웃 -----

    @Test
    fun `refresh로 재발급(200) 후 구 refresh 토큰은 재사용 불가(401)`() {
        seedUser("dave", "password123")
        val first = login("dave", "password123")

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequest(first.refreshToken)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)

        // 회전으로 구 refresh는 폐기됨 → 재사용 401
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequest(first.refreshToken)))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `로그아웃 후 해당 refresh 토큰은 재발급에 사용할 수 없다(401)`() {
        seedUser("erin", "password123")
        val tokens = login("erin", "password123")

        mockMvc.perform(
            post("/api/v1/auth/logout")
                .header("Authorization", bearer(tokens.accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequest(tokens.refreshToken)))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequest(tokens.refreshToken)))
        ).andExpect(status().isUnauthorized)
    }
}
