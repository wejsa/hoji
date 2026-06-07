package com.hoji.security

import com.hoji.config.properties.JwtProperties
import com.hoji.domain.Role
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

/**
 * JWT 발급/검증 컴포넌트. 무상태(서명 검증)로 동작한다.
 */
@Component
class JwtTokenProvider(
    jwtProperties: JwtProperties
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    private val accessTokenValidityMs: Long = jwtProperties.accessTokenValidityMs
    private val refreshTokenValidityMs: Long = jwtProperties.refreshTokenValidityMs

    fun createAccessToken(userId: Long, username: String, role: Role): String =
        buildToken(userId, username, role, accessTokenValidityMs, TYPE_ACCESS)

    fun createRefreshToken(userId: Long, username: String, role: Role): String =
        buildToken(userId, username, role, refreshTokenValidityMs, TYPE_REFRESH)

    private fun buildToken(userId: Long, username: String, role: Role, validityMs: Long, type: String): String {
        val now = Date()
        return Jwts.builder()
            .id(UUID.randomUUID().toString()) // jti — 동일 초에 발급해도 토큰이 유일하도록(회전 무효화 보장)
            .subject(username)
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_ROLE, role.name)
            .claim(CLAIM_TYPE, type)
            .issuedAt(now)
            .expiration(Date(now.time + validityMs))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean = try {
        parseClaims(token)
        true
    } catch (e: JwtException) {
        logger.debug { "Invalid JWT: ${e.message}" }
        false
    } catch (e: IllegalArgumentException) {
        logger.debug { "Empty/blank JWT: ${e.message}" }
        false
    }

    /** 검증된 토큰으로부터 SecurityContext에 주입할 Authentication 생성 */
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val role = claims[CLAIM_ROLE] as String
        val authorities = listOf(SimpleGrantedAuthority("$ROLE_PREFIX$role"))
        val principal = SpringUser(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun getUserId(token: String): Long = (parseClaims(token)[CLAIM_USER_ID] as Number).toLong()

    fun getUsername(token: String): String = parseClaims(token).subject

    /** 토큰 만료 시각. RefreshToken 영속화 시 `expires_at`으로 사용한다. */
    fun getExpiration(token: String): LocalDateTime =
        LocalDateTime.ofInstant(parseClaims(token).expiration.toInstant(), ZoneId.systemDefault())

    /** typ 클레임이 명시적으로 access일 때만 true. typ 부재 토큰은 access로 인정하지 않는다. */
    fun isAccessToken(token: String): Boolean = tokenType(token) == TYPE_ACCESS

    /** 서명·만료가 유효하고 typ=refresh인 Refresh 토큰이면 true. */
    fun isRefreshToken(token: String): Boolean = validateToken(token) && tokenType(token) == TYPE_REFRESH

    /** typ 클레임 원본(부재 시 null). 폴백 없이 명시 값만 반환해 토큰 타입 혼용을 차단한다. */
    private fun tokenType(token: String): String? = parseClaims(token)[CLAIM_TYPE] as? String

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    companion object {
        private const val CLAIM_USER_ID = "uid"
        private const val CLAIM_ROLE = "role"
        private const val CLAIM_TYPE = "typ"
        private const val TYPE_ACCESS = "access"
        private const val TYPE_REFRESH = "refresh"
        private const val ROLE_PREFIX = "ROLE_"
    }
}
