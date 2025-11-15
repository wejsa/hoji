package com.hoji.adapter.`in`.web.dto

import com.hoji.domain.model.User
import com.hoji.domain.model.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 사용자 생성 요청 DTO
 */
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 100, message = "Email must be less than 100 characters")
    val email: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String
)

/**
 * 사용자 수정 요청 DTO
 */
data class UpdateUserRequest(
    @field:Email(message = "Invalid email format")
    @field:Size(max = 100, message = "Email must be less than 100 characters")
    val email: String? = null,

    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null
)

/**
 * 사용자 응답 DTO
 */
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val status: UserStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                username = user.username,
                email = user.email,
                name = user.name,
                status = user.status,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                createdBy = user.createdBy,
                updatedBy = user.updatedBy
            )
        }
    }
}
