package com.hoji.application.service

import com.hoji.common.exception.ConflictException
import com.hoji.common.exception.NotFoundException
import com.hoji.domain.model.User
import com.hoji.domain.port.`in`.*
import com.hoji.domain.port.out.UserPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 사용자 Use Case 구현
 * Application Layer - 비즈니스 로직 조율
 */
@Service
@Transactional
class UserService(
    private val userPort: UserPort
) : CreateUserUseCase,
    GetUserUseCase,
    UpdateUserUseCase,
    DeleteUserUseCase {

    override fun createUser(command: CreateUserCommand): User {
        logger.info { "Creating user with username: ${command.username}" }

        // 중복 검증
        userPort.findByUsername(command.username)?.let {
            throw ConflictException("Username already exists: ${command.username}")
        }

        userPort.findByEmail(command.email)?.let {
            throw ConflictException("Email already exists: ${command.email}")
        }

        // 사용자 생성
        val user = User(
            username = command.username,
            email = command.email,
            name = command.name
        )

        return userPort.save(user).also {
            logger.info { "User created successfully: ${it.id}" }
        }
    }

    @Transactional(readOnly = true)
    override fun getUserById(userId: Long): User {
        logger.debug { "Getting user by id: $userId" }
        return userPort.findById(userId)
            ?: throw NotFoundException("User not found with id: $userId")
    }

    @Transactional(readOnly = true)
    override fun getAllUsers(): List<User> {
        logger.debug { "Getting all users" }
        return userPort.findAll()
    }

    override fun updateUser(userId: Long, command: UpdateUserCommand): User {
        logger.info { "Updating user: $userId" }

        val user = userPort.findById(userId)
            ?: throw NotFoundException("User not found with id: $userId")

        // 이메일 중복 검증
        command.email?.let { newEmail ->
            userPort.findByEmail(newEmail)?.let {
                if (it.id != userId) {
                    throw ConflictException("Email already exists: $newEmail")
                }
            }
            user.email = newEmail
        }

        // 이름 업데이트
        command.name?.let {
            user.name = it
        }

        return userPort.save(user).also {
            logger.info { "User updated successfully: ${it.id}" }
        }
    }

    override fun deleteUser(userId: Long) {
        logger.info { "Deleting user: $userId" }

        if (!userPort.existsById(userId)) {
            throw NotFoundException("User not found with id: $userId")
        }

        userPort.deleteById(userId)
        logger.info { "User deleted successfully: $userId" }
    }
}
