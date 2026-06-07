package com.hoji.repository

import com.hoji.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Refresh Token 레포지토리. 토큰은 SHA-256 해시로 조회/삭제한다.
 */
@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?

    /**
     * 해시 토큰을 단일 DELETE 문으로 폐기하고 삭제된 행 수를 반환한다.
     * 동시 회전 시 행 잠금으로 직렬화되어, 1건만 1을 받고 나머지는 0을 받아 재사용을 차단한다.
     */
    @Modifying
    @Query("delete from RefreshToken r where r.token = :token")
    fun deleteByToken(@Param("token") token: String): Int

    fun deleteByUserId(userId: Long)
}
