package com.hoji.domain.port.`in`

import com.hoji.domain.model.User

/**
 * 사용자 조회 Use Case (Inbound Port)
 */
interface GetUserUseCase {
    /**
     * ID로 사용자 조회
     */
    fun getUserById(userId: Long): User

    /**
     * 모든 사용자 조회
     */
    fun getAllUsers(): List<User>
}
