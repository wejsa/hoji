package com.hoji.domain

import com.hoji.domain.common.BaseEntity
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

    @Column(nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE
) : BaseEntity()

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}
