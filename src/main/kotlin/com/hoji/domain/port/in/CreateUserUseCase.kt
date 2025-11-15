package com.hoji.domain.port.`in`

import com.hoji.domain.model.User

/**
 * 사용자 생성 Use Case (Inbound Port)
 * 헥사고날 아키텍처의 입력 포트
 */
interface CreateUserUseCase {
    fun createUser(command: CreateUserCommand): User
}

/**
 * 사용자 생성 명령
 */
data class CreateUserCommand(
    val username: String,
    val email: String,
    val name: String
)
