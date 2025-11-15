package com.hoji.domain.port.`in`

import com.hoji.domain.model.User

/**
 * 사용자 수정 Use Case (Inbound Port)
 */
interface UpdateUserUseCase {
    fun updateUser(userId: Long, command: UpdateUserCommand): User
}

/**
 * 사용자 수정 명령
 */
data class UpdateUserCommand(
    val email: String? = null,
    val name: String? = null
)
