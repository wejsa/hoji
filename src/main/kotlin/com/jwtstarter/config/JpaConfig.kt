package com.jwtstarter.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * JPA 설정
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["com.jwtstarter.repository"])
class JpaConfig
