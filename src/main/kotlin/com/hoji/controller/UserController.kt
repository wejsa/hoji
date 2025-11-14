package com.hoji.controller

import com.hoji.common.dto.ApiResponse
import com.hoji.controller.dto.CreateUserRequest
import com.hoji.controller.dto.UpdateUserRequest
import com.hoji.controller.dto.UserResponse
import com.hoji.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * 사용자 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    /**
     * 사용자 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserResponse> {
        logger.info { "Creating user: ${request.username}" }
        val user = userService.createUser(request)
        return ApiResponse.success(user, "User created successfully")
    }

    /**
     * 사용자 조회
     */
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> {
        logger.info { "Getting user: $id" }
        val user = userService.getUser(id)
        return ApiResponse.success(user)
    }

    /**
     * 사용자 목록 조회
     */
    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean
    ): ApiResponse<List<UserResponse>> {
        logger.info { "Getting all users (activeOnly: $activeOnly)" }
        val users = if (activeOnly) {
            userService.getActiveUsers()
        } else {
            userService.getAllUsers()
        }
        return ApiResponse.success(users)
    }

    /**
     * 사용자 수정
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ApiResponse<UserResponse> {
        logger.info { "Updating user: $id" }
        val user = userService.updateUser(id, request)
        return ApiResponse.success(user, "User updated successfully")
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "Deleting user: $id" }
        userService.deleteUser(id)
        return ApiResponse.success(message = "User deleted successfully")
    }
}
