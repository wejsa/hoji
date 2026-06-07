package com.hoji.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration

/**
 * 캐시 설정 — 로컬 캐시(Caffeine).
 *
 * CacheManager는 `spring.cache.type=caffeine` + classpath의 Caffeine으로
 * Spring Boot가 자동 구성하며, 본 클래스는 캐시 추상화를 활성화한다.
 */
@Configuration
@EnableCaching
class CacheConfig
