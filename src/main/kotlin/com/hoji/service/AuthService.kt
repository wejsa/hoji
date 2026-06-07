package com.hoji.service

import com.hoji.common.exception.ConflictException
import com.hoji.common.exception.UnauthorizedException
import com.hoji.controller.dto.LoginRequest
import com.hoji.controller.dto.SignupRequest
import com.hoji.controller.dto.SignupResponse
import com.hoji.controller.dto.TokenResponse
import com.hoji.domain.Role
import com.hoji.domain.User
import com.hoji.repository.UserRepository
import com.hoji.security.JwtTokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 인증 서비스 — 회원가입/로그인.
 *
 * 비밀번호는 BCrypt로 해싱해 저장하며 평문을 로깅하지 않는다. 로그인은 Spring
 * [AuthenticationManager]로 자격증명을 검증(비활성 계정 차단 포함)한 뒤 JWT를 발급한다.
 */
@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    /**
     * 회원가입. username/email 중복을 검증하고 USER 권한으로 생성한다.
     */
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        logger.info { "Signup attempt: ${request.username}" }

        if (userRepository.existsByUsername(request.username)) {
            throw ConflictException("Username already exists: ${request.username}")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already exists: ${request.email}")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = Role.USER
        )
        val saved = userRepository.save(user)
        logger.info { "User registered with id: ${saved.id}" }

        return SignupResponse.from(saved)
    }

    /**
     * 로그인. 자격증명 검증 성공 시 Access/Refresh 토큰을 발급한다.
     * 실패(자격증명 불일치/비활성 계정)는 정보 노출을 막기 위해 동일한 401로 응답한다.
     */
    fun login(request: LoginRequest): TokenResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
        } catch (e: AuthenticationException) {
            logger.info { "Login failed for ${request.username}: ${e.javaClass.simpleName}" }
            throw UnauthorizedException("Invalid username or password")
        }

        val user = userRepository.findByUsername(request.username)
            ?: throw UnauthorizedException("Invalid username or password")

        val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.username, user.role)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id!!, user.username, user.role)
        logger.info { "Login success: ${user.username}" }

        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }
}
