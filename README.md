# Hoji - Spring Boot Backend Base Project

Kotlin + Spring Boot 기반 백엔드 베이스 프로젝트

## 기술 스택

### Core
- **Java**: 17
- **Kotlin**: 1.9.20
- **Spring Boot**: 3.2.0
- **Gradle**: 8.5 (Kotlin DSL)

### Database & ORM
- **H2**: 인메모리 데이터베이스 (개발/테스트용)
- **JPA**: 데이터 액세스
- **QueryDSL**: 타입 안전 쿼리
- **Spring Data JPA**: 레포지토리 추상화

### Message Queue & Cache
- **RabbitMQ**: 메시지 큐
- **Redis**: 캐시 및 세션 저장소

### HTTP Client
- **WebClient**: 비동기 HTTP 클라이언트
- **RestTemplate**: 동기 HTTP 클라이언트

### Logging
- **Kotlin Logging**: Kotlin 친화적 로깅
- **Logback**: 로깅 프레임워크

### Documentation
- **SpringDoc OpenAPI**: API 문서화 (Swagger)

### Testing
- **JUnit 5**: 테스트 프레임워크
- **MockK**: Kotlin Mocking 라이브러리
- **SpringMockK**: Spring + MockK 통합

## 주요 기능

### 1단계 - 핵심 기능
- ✅ Spring Boot + Gradle + Kotlin 프로젝트 구조
- ✅ 공통 응답 구조 (ApiResponse)
- ✅ 전역 Exception Handler (@ControllerAdvice)
- ✅ H2 + JPA 기본 설정
- ✅ Health Check API

### 2단계 - 고급 기능
- ✅ 요청/응답 로깅 인터셉터 (상세 로깅)
- ✅ 헤더 컨텍스트 관리 (ThreadLocal 기반)
- ✅ QueryDSL 설정
- ✅ Redis 설정 (선택적)
- ✅ RabbitMQ 설정 (선택적)
- ✅ WebClient 및 RestTemplate 설정
- ✅ Logback 설정 (파일 로깅, 로테이션)
- ✅ 예제 도메인 (User CRUD API)
- ✅ 테스트 코드

## 프로젝트 구조

```
src/main/kotlin/com/hoji/
├── HojiApplication.kt              # 애플리케이션 진입점
├── common/                         # 공통 모듈
│   ├── dto/                       # 공통 DTO
│   │   ├── ApiResponse.kt         # API 응답 구조
│   │   └── ResultCode.kt          # 응답 코드 정의
│   ├── exception/                 # 예외 관리
│   │   ├── BusinessException.kt   # 비즈니스 예외
│   │   └── GlobalExceptionHandler.kt  # 전역 예외 처리
│   ├── logging/                   # 로깅
│   │   ├── LoggingInterceptor.kt  # 로깅 인터셉터
│   │   └── RequestResponseCachingFilter.kt  # 요청/응답 캐싱 필터
│   └── context/                   # 컨텍스트 관리
│       ├── RequestContext.kt      # 요청 컨텍스트
│       └── RequestContextInterceptor.kt  # 컨텍스트 인터셉터
├── config/                        # 설정
│   ├── JpaConfig.kt              # JPA 설정
│   ├── QueryDslConfig.kt         # QueryDSL 설정
│   ├── RedisConfig.kt            # Redis 설정
│   ├── RabbitMqConfig.kt         # RabbitMQ 설정
│   ├── WebClientConfig.kt        # WebClient 설정
│   ├── RestTemplateConfig.kt     # RestTemplate 설정
│   └── WebMvcConfig.kt           # Web MVC 설정
├── controller/                    # 컨트롤러
│   ├── HealthController.kt       # Health Check
│   ├── UserController.kt         # 사용자 API
│   └── dto/                      # DTO
│       └── UserDto.kt            # 사용자 DTO
├── service/                       # 서비스
│   └── UserService.kt            # 사용자 서비스
├── domain/                        # 도메인 엔티티
│   └── User.kt                   # 사용자 엔티티
└── repository/                    # 레포지토리
    └── UserRepository.kt         # 사용자 레포지토리
```

## 빌드 및 실행

### 요구사항
- JDK 17 이상

### 빌드
```bash
./gradlew build
```

### 테스트
```bash
./gradlew test
```

### 실행
```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/hoji-0.0.1-SNAPSHOT.jar
```

## API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## API 엔드포인트

### Health Check
```
GET /api/v1/health
```

### User API
```
POST   /api/v1/users              # 사용자 생성
GET    /api/v1/users              # 사용자 목록 조회
GET    /api/v1/users/{id}         # 사용자 조회
PUT    /api/v1/users/{id}         # 사용자 수정
DELETE /api/v1/users/{id}         # 사용자 삭제
```

### Actuator
```
GET /actuator/health              # 헬스 체크
GET /actuator/info                # 애플리케이션 정보
GET /actuator/metrics             # 메트릭
```

## H2 Console

개발 환경에서 H2 콘솔에 접근할 수 있습니다:

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:hoji`
- Username: `sa`
- Password: (비어있음)

## 환경 설정

### Redis 및 RabbitMQ 비활성화

Redis나 RabbitMQ를 사용하지 않는 경우, `application.yml`에서 다음을 추가하세요:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

또는 해당 설정의 `@ConditionalOnProperty`에 의해 자동으로 비활성화됩니다.

## 로깅

- Console 로그: 개발 환경에서 활성화
- File 로그: `logs/hoji.log`
- Error 로그: `logs/hoji-error.log`
- 로그 파일은 일별로 로테이션되며, 최대 30일간 보관됩니다.

## 주요 특징

### 1. 공통 응답 구조
모든 API는 일관된 응답 구조를 사용합니다:
```json
{
  "success": true,
  "code": "0000",
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

### 2. 전역 예외 처리
`@RestControllerAdvice`를 통해 모든 예외를 일관되게 처리합니다.

### 3. 요청/응답 로깅
모든 HTTP 요청과 응답을 자동으로 로깅합니다 (민감한 헤더는 마스킹).

### 4. 헤더 컨텍스트
`X-Request-ID`, `X-User-ID` 등의 헤더를 ThreadLocal에 저장하여 전역에서 접근 가능합니다.

### 5. QueryDSL
타입 안전한 쿼리를 작성할 수 있습니다.

## 라이센스

MIT License
