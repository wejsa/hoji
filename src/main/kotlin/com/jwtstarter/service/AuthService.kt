package com.jwtstarter.service

import com.jwtstarter.common.exception.ConflictException
import com.jwtstarter.common.exception.NotFoundException
import com.jwtstarter.common.exception.UnauthorizedException
import com.jwtstarter.controller.dto.LoginRequest
import com.jwtstarter.controller.dto.MeResponse
import com.jwtstarter.controller.dto.RefreshRequest
import com.jwtstarter.controller.dto.SignupRequest
import com.jwtstarter.controller.dto.SignupResponse
import com.jwtstarter.controller.dto.TokenResponse
import com.jwtstarter.domain.RefreshToken
import com.jwtstarter.domain.Role
import com.jwtstarter.domain.User
import com.jwtstarter.domain.UserStatus
import com.jwtstarter.repository.RefreshTokenRepository
import com.jwtstarter.repository.UserRepository
import com.jwtstarter.security.JwtTokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest

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
    private val refreshTokenRepository: RefreshTokenRepository,
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
    @Transactional
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

        logger.info { "Login success: ${user.username}" }
        return issueTokens(user)
    }

    /**
     * Refresh Token으로 Access/Refresh를 재발급한다(회전: 기존 RT 폐기 후 신규 발급).
     * 서명·typ·만료가 유효하고 DB에 저장된(미폐기) 토큰일 때만 성공한다.
     * 위조/만료/로그아웃(폐기)된 토큰은 401로 거부한다.
     */
    @Transactional
    fun refresh(request: RefreshRequest): TokenResponse {
        val token = request.refreshToken
        if (!jwtTokenProvider.isRefreshToken(token)) {
            throw UnauthorizedException("Invalid refresh token")
        }

        // 회전 + 존재검증을 단일 DELETE로 처리한다. 삭제 0건이면 미저장/이미폐기/동시회전 패자 → 거부.
        if (refreshTokenRepository.deleteByToken(hashToken(token)) == 0) {
            throw UnauthorizedException("Invalid refresh token")
        }

        val user = userRepository.findByUsername(jwtTokenProvider.getUsername(token))
            ?: throw UnauthorizedException("Invalid refresh token")
        // 비활성/삭제 계정은 재발급을 거부한다(로그인의 비활성 차단과 동일 정책). RT는 이미 폐기됨.
        if (user.status != UserStatus.ACTIVE) {
            logger.info { "Refresh denied for non-active account: ${user.username}" }
            throw UnauthorizedException("Invalid refresh token")
        }

        logger.info { "Token refreshed: ${user.username}" }
        return issueTokens(user)
    }

    /**
     * 로그아웃. 보유한 Refresh Token을 DB에서 폐기해 이후 재발급을 차단한다.
     * 유효하지 않은 토큰은 멱등하게 무시한다(이미 폐기된 것과 동일 효과).
     */
    @Transactional
    fun logout(request: RefreshRequest) {
        refreshTokenRepository.deleteByToken(hashToken(request.refreshToken))
        logger.info { "Logout processed" }
    }

    /**
     * 현재 인증된 사용자 정보를 조회한다.
     */
    fun me(username: String): MeResponse {
        val user = userRepository.findByUsername(username)
            ?: throw NotFoundException("User not found: $username")
        return MeResponse.from(user)
    }

    /** Access/Refresh를 발급하고 Refresh의 SHA-256 해시를 영속화한다. */
    private fun issueTokens(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.username, user.role)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.username, user.role)

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                token = hashToken(refreshToken),
                expiresAt = jwtTokenProvider.getExpiration(refreshToken),
            )
        )
        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    /** Refresh 토큰 평문을 SHA-256 hex로 해시한다(DB에는 해시만 저장). */
    private fun hashToken(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
