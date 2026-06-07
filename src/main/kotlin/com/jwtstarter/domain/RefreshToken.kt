package com.jwtstarter.domain

import com.jwtstarter.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Refresh Token 엔티티.
 *
 * 평문 토큰 대신 SHA-256 해시를 저장한다(DB 유출 시 토큰 원문 보호). 로그아웃/재발급 회전 시
 * 무효화할 수 있도록 DB에 영속화한다(무상태 JWT만으로는 서버측 폐기가 불가).
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")],
)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    /** Refresh 토큰의 SHA-256 해시(hex). 평문은 저장하지 않는다. */
    @Column(nullable = false, unique = true, length = 255)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity()
