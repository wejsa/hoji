package com.jwtstarter.repository

import com.jwtstarter.domain.User
import com.jwtstarter.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사용자 레포지토리
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByStatus(status: UserStatus): List<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
