package com.hoji.adapter.out.persistence

import com.hoji.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * JPA Repository
 * Spring Data JPA 인터페이스
 */
interface UserJpaRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
}
