package com.hoji.domain.port.`in`

/**
 * 사용자 삭제 Use Case (Inbound Port)
 */
interface DeleteUserUseCase {
    fun deleteUser(userId: Long)
}
