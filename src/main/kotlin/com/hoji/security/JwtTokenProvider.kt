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
import java.util.Date
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
        buildToken(userId, username, role, accessTokenValidityMs)

    fun createRefreshToken(userId: Long, username: String, role: Role): String =
        buildToken(userId, username, role, refreshTokenValidityMs)

    private fun buildToken(userId: Long, username: String, role: Role, validityMs: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(username)
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_ROLE, role.name)
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

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    companion object {
        private const val CLAIM_USER_ID = "uid"
        private const val CLAIM_ROLE = "role"
        private const val ROLE_PREFIX = "ROLE_"
    }
}
