package com.hoji.config

import com.hoji.security.JwtAccessDeniedHandler
import com.hoji.security.JwtAuthenticationEntryPoint
import com.hoji.security.JwtAuthenticationFilter
import com.hoji.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정 — 무상태 JWT 인증.
 *
 * 인증/인가 라우팅의 기본 골격을 정의한다. 엔드포인트별 세부 권한(@PreAuthorize)은
 * Step 5에서 보강한다.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val accessDeniedHandler: JwtAccessDeniedHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(*PUBLIC_PATHS).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
        configuration.authenticationManager

    companion object {
        private val PUBLIC_PATHS = arrayOf(
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/actuator/health/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        )
    }
}
