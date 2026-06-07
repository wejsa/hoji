package com.hoji.service

import com.hoji.common.exception.ConflictException
import com.hoji.common.exception.NotFoundException
import com.hoji.controller.dto.CreateUserRequest
import com.hoji.controller.dto.UpdateUserRequest
import com.hoji.controller.dto.UserResponse
import com.hoji.domain.User
import com.hoji.domain.UserStatus
import com.hoji.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 사용자 서비스
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    // Step 2에서 SecurityConfig의 PasswordEncoder 빈 주입으로 대체 예정
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    /**
     * 사용자 생성
     */
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        logger.info { "Creating user with username: ${request.username}" }

        // 중복 체크
        if (userRepository.existsByUsername(request.username)) {
            throw ConflictException("Username already exists: ${request.username}")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already exists: ${request.email}")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name
        )

        val savedUser = userRepository.save(user)
        logger.info { "User created with id: ${savedUser.id}" }

        return UserResponse.from(savedUser)
    }

    /**
     * 사용자 조회
     */
    fun getUser(id: Long): UserResponse {
        logger.info { "Getting user with id: $id" }
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User not found with id: $id")
        return UserResponse.from(user)
    }

    /**
     * 사용자 목록 조회
     */
    fun getAllUsers(): List<UserResponse> {
        logger.info { "Getting all users" }
        return userRepository.findAll().map { UserResponse.from(it) }
    }

    /**
     * 활성 사용자 목록 조회
     */
    fun getActiveUsers(): List<UserResponse> {
        logger.info { "Getting active users" }
        return userRepository.findByStatus(UserStatus.ACTIVE)
            .map { UserResponse.from(it) }
    }

    /**
     * 사용자 수정
     */
    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        logger.info { "Updating user with id: $id" }

        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User not found with id: $id")

        request.email?.let {
            if (it != user.email && userRepository.existsByEmail(it)) {
                throw ConflictException("Email already exists: $it")
            }
            user.email = it
        }

        request.name?.let { user.name = it }
        request.status?.let { user.status = it }

        val updatedUser = userRepository.save(user)
        logger.info { "User updated with id: ${updatedUser.id}" }

        return UserResponse.from(updatedUser)
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    fun deleteUser(id: Long) {
        logger.info { "Deleting user with id: $id" }

        if (!userRepository.existsById(id)) {
            throw NotFoundException("User not found with id: $id")
        }

        userRepository.deleteById(id)
        logger.info { "User deleted with id: $id" }
    }
}
