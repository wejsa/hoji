package com.jwtstarter.controller.dto

import com.jwtstarter.domain.Role
import com.jwtstarter.domain.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 회원가입 요청 DTO
 */
data class SignupRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String
)

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * 토큰 응답 DTO (로그인/재발급 공용)
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

/**
 * Refresh Token 요청 DTO (재발급/로그아웃 공용)
 */
data class RefreshRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * 현재 사용자 조회 응답 DTO. 비밀번호 등 민감 필드는 노출하지 않는다.
 */
data class MeResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val role: Role
) {
    companion object {
        fun from(user: User): MeResponse = MeResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            name = user.name,
            role = user.role
        )
    }
}

/**
 * 회원가입 응답 DTO. 비밀번호 등 민감 필드는 노출하지 않는다.
 */
data class SignupResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val role: Role
) {
    companion object {
        fun from(user: User): SignupResponse = SignupResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            name = user.name,
            role = user.role
        )
    }
}
