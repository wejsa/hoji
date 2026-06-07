package com.hoji.repository

import com.hoji.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Refresh Token 레포지토리. 토큰은 SHA-256 해시로 조회/삭제한다.
 */
@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun deleteByToken(token: String)
    fun deleteByUserId(userId: Long)
}
