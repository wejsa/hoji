package com.jwtstarter.domain

import com.jwtstarter.domain.common.BaseEntity
import jakarta.persistence.*

/**
 * 사용자 엔티티 (예제)
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

    /** BCrypt 해시 (고정 60자). 평문을 저장하지 않는다. */
    @Column(nullable = false, length = 60)
    var password: String,

    @Column(nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE
) : BaseEntity()

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}
