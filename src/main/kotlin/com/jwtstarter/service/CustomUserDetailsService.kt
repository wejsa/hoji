package com.jwtstarter.service

import com.jwtstarter.domain.UserStatus
import com.jwtstarter.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Spring Security 인증용 UserDetailsService.
 *
 * username으로 사용자를 조회해 인증 주체(UserDetails)로 변환한다. ACTIVE가 아닌 계정은
 * disabled 처리되어 [org.springframework.security.authentication.AuthenticationManager]가
 * 인증을 거부한다(로그인 차단). 권한은 `ROLE_` 접두사를 부여한다.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return SpringUser.builder()
            .username(user.username)
            .password(user.password)
            .authorities(SimpleGrantedAuthority("$ROLE_PREFIX${user.role.name}"))
            .disabled(user.status != UserStatus.ACTIVE)
            .build()
    }

    companion object {
        private const val ROLE_PREFIX = "ROLE_"
    }
}
