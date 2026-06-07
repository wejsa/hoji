package com.hoji.controller

import com.hoji.common.dto.ApiResponse
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.MeResponse
import com.hoji.controller.dto.RefreshRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.controller.dto.SignupResponse
import com.hoji.controller.dto.TokenResponse
import com.hoji.service.AuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

/**
 * 인증 API 컨트롤러 (회원가입/로그인). 라우팅은 SecurityConfig PUBLIC_PATHS에서 허용된다.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Valid @RequestBody request: SignupRequest): ApiResponse<SignupResponse> {
        logger.info { "Signup request: ${request.username}" }
        return ApiResponse.success(authService.signup(request), "Signup successful")
    }

    /**
     * 로그인 — Access/Refresh 토큰 발급
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> {
        logger.info { "Login request: ${request.username}" }
        return ApiResponse.success(authService.login(request), "Login successful")
    }

    /**
     * 토큰 재발급 — 유효한 Refresh Token으로 Access/Refresh를 회전 발급. (PUBLIC_PATHS 허용)
     */
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ApiResponse<TokenResponse> {
        logger.info { "Token refresh request" }
        return ApiResponse.success(authService.refresh(request), "Token refreshed")
    }

    /**
     * 로그아웃 — 보유 Refresh Token 폐기 (인증 필요)
     */
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshRequest): ApiResponse<Unit> {
        authService.logout(request)
        return ApiResponse.success(Unit, "Logout successful")
    }

    /**
     * 현재 사용자 조회 (인증 필요)
     */
    @GetMapping("/me")
    fun me(authentication: Authentication): ApiResponse<MeResponse> =
        ApiResponse.success(authService.me(authentication.name))
}
