package com.jwtstarter.security

import com.jwtstarter.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/**
 * 메서드 보안(@PreAuthorize) SpEL에서 사용하는 소유권 판정 빈.
 *
 * 인증 주체(JWT subject=username)가 대상 리소스(userId)의 소유자인지 검사한다.
 * 예: `@PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id, authentication)")`
 */
@Component("userSecurity")
class UserSecurity(
    private val userRepository: UserRepository
) {
    /** 인증 주체가 path의 userId 본인이면 true. 미인증/미존재/불일치는 false. */
    fun isSelf(userId: Long, authentication: Authentication?): Boolean {
        val username = authentication?.name ?: return false
        val user = userRepository.findByUsername(username) ?: return false
        return user.id == userId
    }
}
