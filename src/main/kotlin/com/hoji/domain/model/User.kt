package com.hoji.domain.model

import com.hoji.domain.common.BaseEntity
import jakarta.persistence.*

/**
 * 사용자 도메인 모델
 * 헥사고날 아키텍처의 Core Domain
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    var username: String,

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE
) : BaseEntity() {

    fun activate() {
        this.status = UserStatus.ACTIVE
    }

    fun deactivate() {
        this.status = UserStatus.INACTIVE
    }

    fun suspend() {
        this.status = UserStatus.SUSPENDED
    }

    fun isActive(): Boolean = status == UserStatus.ACTIVE
}

/**
 * 사용자 상태
 */
enum class UserStatus {
    ACTIVE,    // 활성
    INACTIVE,  // 비활성
    SUSPENDED  // 정지
}
