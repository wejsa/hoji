package com.hoji.adapter.`in`.web

import com.hoji.adapter.`in`.web.dto.CreateUserRequest
import com.hoji.adapter.`in`.web.dto.UpdateUserRequest
import com.hoji.adapter.`in`.web.dto.UserResponse
import com.hoji.common.dto.ApiResponse
import com.hoji.domain.port.`in`.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * 사용자 Web Adapter (Inbound Adapter)
 * REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) {

    /**
     * 사용자 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserResponse> {
        logger.info { "POST /api/v1/users - Create user: ${request.username}" }

        val command = CreateUserCommand(
            username = request.username,
            email = request.email,
            name = request.name
        )

        val user = createUserUseCase.createUser(command)
        return ApiResponse.success(UserResponse.from(user), "User created successfully")
    }

    /**
     * 모든 사용자 조회
     */
    @GetMapping
    fun getAllUsers(): ApiResponse<List<UserResponse>> {
        logger.info { "GET /api/v1/users - Get all users" }

        val users = getUserUseCase.getAllUsers()
        return ApiResponse.success(users.map { UserResponse.from(it) })
    }

    /**
     * 사용자 조회
     */
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> {
        logger.info { "GET /api/v1/users/$id - Get user" }

        val user = getUserUseCase.getUserById(id)
        return ApiResponse.success(UserResponse.from(user))
    }

    /**
     * 사용자 수정
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ApiResponse<UserResponse> {
        logger.info { "PUT /api/v1/users/$id - Update user" }

        val command = UpdateUserCommand(
            email = request.email,
            name = request.name
        )

        val user = updateUserUseCase.updateUser(id, command)
        return ApiResponse.success(UserResponse.from(user), "User updated successfully")
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/users/$id - Delete user" }

        deleteUserUseCase.deleteUser(id)
        return ApiResponse.success(message = "User deleted successfully")
    }
}
