package com.jwtstarter.domain

/**
 * 사용자 권한
 *
 * Spring Security 연동 시 `ROLE_` 접두사를 부여해 GrantedAuthority로 변환한다.
 * DB에는 접두사 없이 enum 이름(USER/ADMIN)을 STRING으로 저장한다.
 */
enum class Role {
    USER,
    ADMIN
}
