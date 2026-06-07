package com.hoji.controller

import com.hoji.common.dto.ApiResponse
import com.hoji.common.exception.ForbiddenException
import com.hoji.controller.dto.CreateUserRequest
import com.hoji.controller.dto.UpdateUserRequest
import com.hoji.controller.dto.UserResponse
import com.hoji.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * 사용자 API 컨트롤러.
 *
 * 접근 제어(Step 5): 생성/목록/삭제는 ADMIN 전용, 단건 조회/수정은 ADMIN 또는 본인만 허용한다.
 * 인증 자체는 SecurityConfig(anyRequest authenticated)가 강제하며, 여기서는 권한(RBAC)을 좁힌다.
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    /**
     * 사용자 생성 (ADMIN 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserResponse> {
        logger.info { "Creating user: ${request.username}" }
        val user = userService.createUser(request)
        return ApiResponse.success(user, "User created successfully")
    }

    /**
     * 사용자 조회 (ADMIN 또는 본인)
     */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id, authentication)")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> {
        logger.info { "Getting user: $id" }
        val user = userService.getUser(id)
        return ApiResponse.success(user)
    }

    /**
     * 사용자 목록 조회 (ADMIN 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
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
     * 사용자 수정 (ADMIN 또는 본인).
     *
     * `status`는 운영자가 통제하는 특권 상태머신 필드이므로 ADMIN만 변경할 수 있다.
     * 본인(USER) self-edit으로 자기 계정을 INACTIVE/DELETED로 전이시키는 권한 우회를 차단한다.
     */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id, authentication)")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
        authentication: Authentication?
    ): ApiResponse<UserResponse> {
        logger.info { "Updating user: $id" }
        if (request.status != null && authentication?.authorities?.any { it.authority == ROLE_ADMIN } != true) {
            throw ForbiddenException("Only ADMIN can change user status")
        }
        val user = userService.updateUser(id, request)
        return ApiResponse.success(user, "User updated successfully")
    }

    /**
     * 사용자 삭제 (ADMIN 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "Deleting user: $id" }
        userService.deleteUser(id)
        return ApiResponse.success(message = "User deleted successfully")
    }

    companion object {
        private const val ROLE_ADMIN = "ROLE_ADMIN"
    }
}
