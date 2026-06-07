package com.hoji.controller

import com.hoji.common.dto.ApiResponse
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.controller.dto.SignupResponse
import com.hoji.controller.dto.TokenResponse
import com.hoji.service.AuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
}
