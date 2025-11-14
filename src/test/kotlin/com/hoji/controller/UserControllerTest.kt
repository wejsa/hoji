package com.hoji.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hoji.controller.dto.CreateUserRequest
import com.hoji.controller.dto.UpdateUserRequest
import com.hoji.controller.dto.UserResponse
import com.hoji.domain.UserStatus
import com.hoji.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(UserController::class)
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
        val request = UpdateUserRequest(
            email = "newemail@example.com",
            name = "New Name",
            status = UserStatus.INACTIVE
        )
        val response = UserResponse(
            id = userId,
            username = "testuser",
            email = "newemail@example.com",
            name = "New Name",
            status = UserStatus.INACTIVE,
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
