package com.jwtstarter.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 설정
 */
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .servers(servers())
            .components(securitySchemes())
            .addSecurityItem(SecurityRequirement().addList("Bearer Authentication"))
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Spring Boot JWT Starter API")
            .description("""
                spring-boot-jwt-starter API 문서

                ## 주요 기능
                - RESTful API
                - JWT 인증/인가 (Spring Security + JWT)
                - 공통 응답 구조
                - 에러 처리

                ## 사용 방법
                1. 인증이 필요한 API는 헤더에 Authorization: Bearer {token}을 추가
                2. 모든 응답은 공통 ApiResponse 구조를 따름
            """.trimIndent())
            .version("v1.0.0")
            .contact(Contact()
                .name("spring-boot-jwt-starter")
                .email("contact@example.com"))
            .license(License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"))
    }

    private fun servers(): List<Server> {
        return listOf(
            Server().url("/").description("Current Server"),
            Server().url("http://localhost:8080").description("Local Server"),
            Server().url("https://dev.example.com").description("Dev Server"),
            Server().url("https://api.example.com").description("Production Server")
        )
    }

    private fun securitySchemes(): Components {
        return Components()
            .addSecuritySchemes(
                "Bearer Authentication",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .`in`(SecurityScheme.In.HEADER)
                    .name("Authorization")
                    .description("JWT 토큰을 입력하세요 (Bearer 제외)")
            )
    }
}
