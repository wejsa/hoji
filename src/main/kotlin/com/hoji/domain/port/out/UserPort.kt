package com.hoji.domain.port.out

import com.hoji.domain.model.User

/**
 * 사용자 Outbound Port
 * 헥사고날 아키텍처의 출력 포트 (Persistence 추상화)
 */
interface UserPort {
    /**
     * 사용자 저장
     */
    fun save(user: User): User

    /**
     * ID로 사용자 조회
     */
    fun findById(id: Long): User?

    /**
     * username으로 사용자 조회
     */
    fun findByUsername(username: String): User?

    /**
     * email로 사용자 조회
     */
    fun findByEmail(email: String): User?

    /**
     * 모든 사용자 조회
     */
    fun findAll(): List<User>

    /**
     * 사용자 삭제
     */
    fun deleteById(id: Long)

    /**
     * 사용자 존재 여부 확인
     */
    fun existsById(id: Long): Boolean
}
