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

### 3단계 - 상용 서비스 운영 기능
- ✅ JPA Auditing (BaseEntity)
- ✅ 커스텀 메트릭 수집 (Micrometer + Prometheus)
- ✅ 멀티 인스턴스 메트릭 관리
- ✅ 비동기 처리 (Async)
- ✅ CORS 설정
- ✅ Graceful Shutdown
- ✅ 유틸리티 (DateTimeUtils, JsonUtils)
- ✅ Swagger 커스터마이징

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
│   ├── context/                   # 컨텍스트 관리
│   │   ├── RequestContext.kt      # 요청 컨텍스트
│   │   └── RequestContextInterceptor.kt  # 컨텍스트 인터셉터
│   ├── metrics/                   # 메트릭
│   │   ├── CustomMetrics.kt       # 커스텀 메트릭
│   │   └── MetricsInterceptor.kt  # 메트릭 인터셉터
│   ├── health/                    # 헬스 체크
│   │   └── CustomHealthIndicator.kt  # 커스텀 헬스 체크
│   └── util/                      # 유틸리티
│       ├── DateTimeUtils.kt       # 날짜/시간 유틸
│       └── JsonUtils.kt           # JSON 유틸
├── config/                        # 설정
│   ├── JpaConfig.kt              # JPA 설정
│   ├── AuditConfig.kt            # JPA Auditing 설정
│   ├── QueryDslConfig.kt         # QueryDSL 설정
│   ├── RedisConfig.kt            # Redis 설정
│   ├── RabbitMqConfig.kt         # RabbitMQ 설정
│   ├── WebClientConfig.kt        # WebClient 설정
│   ├── RestTemplateConfig.kt     # RestTemplate 설정
│   ├── WebMvcConfig.kt           # Web MVC 설정
│   ├── MetricsConfig.kt          # 메트릭 설정 (멀티 인스턴스)
│   ├── AsyncConfig.kt            # 비동기 설정
│   ├── CorsConfig.kt             # CORS 설정
│   ├── SwaggerConfig.kt          # Swagger 설정
│   └── properties/               # Properties
│       └── LoggingProperties.kt  # 로깅 설정
├── controller/                    # 컨트롤러
│   ├── UserController.kt         # 사용자 API
│   └── dto/                      # DTO
│       └── UserDto.kt            # 사용자 DTO
├── service/                       # 서비스
│   └── UserService.kt            # 사용자 서비스
├── domain/                        # 도메인 엔티티
│   ├── common/                   # 공통 엔티티
│   │   └── BaseEntity.kt         # 베이스 엔티티 (Auditing)
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

#### 기본 실행 (기본 프로파일)
```bash
./gradlew bootRun
```

#### 환경별 실행
```bash
# Local 환경
./gradlew bootRun --args='--spring.profiles.active=local'

# Dev 환경
./gradlew bootRun --args='--spring.profiles.active=dev'

# Prod 환경
./gradlew bootRun --args='--spring.profiles.active=prod'
```

#### JAR 실행
```bash
# 빌드
./gradlew build

# 환경별 실행
java -jar build/libs/hoji-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
java -jar build/libs/hoji-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar build/libs/hoji-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## API 엔드포인트

### Health Check (Actuator)
```
GET /actuator/health              # 전체 헬스 체크
GET /actuator/health/liveness     # Kubernetes Liveness Probe
GET /actuator/health/readiness    # Kubernetes Readiness Probe
```

응답 예시:
```json
{
  "status": "UP",
  "components": {
    "custom": {
      "status": "UP",
      "details": {
        "application": "hoji",
        "status": "healthy",
        "timestamp": "2024-01-01T00:00:00"
      }
    },
    "db": {
      "status": "UP"
    }
  }
}
```

### User API
```
POST   /api/v1/users              # 사용자 생성
GET    /api/v1/users              # 사용자 목록 조회
GET    /api/v1/users/{id}         # 사용자 조회
PUT    /api/v1/users/{id}         # 사용자 수정
DELETE /api/v1/users/{id}         # 사용자 삭제
```

### Actuator (기타)
```
GET /actuator/info                # 애플리케이션 정보
GET /actuator/metrics             # 메트릭
GET /actuator/prometheus          # Prometheus 메트릭
```

**참고**: Actuator 엔드포인트 노출은 환경별로 다릅니다.
- Local: 모든 엔드포인트 노출
- Dev: health, info, metrics, prometheus, env, loggers
- Prod: health, info, metrics, prometheus (최소)

## H2 Console

H2 콘솔 접근 (환경별로 다름):

**Local/Dev 환경**:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:hoji`
- Username: `sa`
- Password: (비어있음)

**Prod 환경**: 보안을 위해 비활성화됨

## 환경별 설정

### 프로파일 구성

프로젝트는 다음 환경별 설정 파일을 제공합니다:

| 파일 | 환경 | 설명 |
|------|------|------|
| `application.yml` | 공통 | 모든 환경에서 사용하는 기본 설정 |
| `application-local.yml` | Local | 로컬 개발 환경 (H2 콘솔, 상세 로깅, 전체 스택트레이스) |
| `application-dev.yml` | Dev | 개발 서버 (H2 콘솔, 적당한 로깅) |
| `application-prod.yml` | Prod | 운영 서버 (보안 강화, 최소 로깅, 에러 정보 비노출) |

### 환경별 주요 차이점

#### Local 환경
- H2 콘솔: 활성화
- 로깅 레벨: DEBUG (상세)
- SQL 로깅: 콘솔 출력
- 에러 스택트레이스: 항상 노출
- Actuator: 모든 엔드포인트 노출
- 민감정보 마스킹: 최소화

#### Dev 환경
- H2 콘솔: 활성화 (외부 접근 차단)
- 로깅 레벨: DEBUG
- SQL 로깅: 파일만
- 에러 스택트레이스: 파라미터로 제어
- Actuator: 주요 엔드포인트만 노출
- 민감정보 마스킹: 표준

#### Prod 환경
- H2 콘솔: 비활성화
- 로깅 레벨: WARN/INFO (최소)
- SQL 로깅: 비활성화
- 에러 스택트레이스: 절대 노출 안함
- Actuator: 최소 엔드포인트만 노출
- 민감정보 마스킹: 강화
- 응답 압축: 활성화
- HTTP/2: 활성화

### Redis 및 RabbitMQ 비활성화

기본적으로 Redis와 RabbitMQ 자동 설정이 제외되어 있습니다.
사용하려면 `application.yml`에서 해당 설정의 `autoconfigure.exclude`를 제거하세요.

## 로깅

### 로그 파일
- 일반 로그: `logs/hoji.log` (또는 환경별 `logs/hoji-{profile}.log`)
- 에러 로그: `logs/hoji-error.log`
- 로테이션: 10MB 단위로 파일 분할
- 보관 기간: 최대 30일

### 환경별 로깅 레벨
- **Local**: DEBUG (상세), SQL 콘솔 출력
- **Dev**: DEBUG, SQL 파일 출력
- **Prod**: WARN/INFO (최소), SQL 비활성화

### 민감정보 마스킹
`application.yml`에서 설정 가능:
```yaml
hoji:
  logging:
    sensitive-headers:
      - authorization
      - cookie
      - x-api-key
      # ... 추가 가능
```

환경별로 다른 마스킹 정책 적용 가능합니다.

## 주요 특징

### 1. 환경별 설정 관리
- Local, Dev, Prod 환경별로 독립적인 설정 파일
- 프로파일 기반 자동 설정 적용
- 환경별 보안 정책 및 로깅 레벨 관리

### 2. Spring Boot Actuator 헬스 체크
- 표준 HealthIndicator 인터페이스 구현
- Kubernetes liveness/readiness 프로브 지원
- 커스텀 헬스 체크 로직 확장 가능
- DB, Redis 등 자동 헬스 체크 통합

### 3. 공통 응답 구조
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

### 4. 전역 예외 처리
`@RestControllerAdvice`를 통해 모든 예외를 일관되게 처리합니다.

### 5. 요청/응답 로깅
모든 HTTP 요청과 응답을 자동으로 로깅합니다:
- 민감한 헤더는 자동 마스킹 (설정 가능)
- 요청/응답 본문 캐싱 및 로깅
- 처리 시간 측정

### 6. 헤더 컨텍스트 관리
`X-Request-ID`, `X-User-ID` 등의 헤더를 ThreadLocal에 저장하여 전역에서 접근 가능합니다.

### 7. QueryDSL
타입 안전한 쿼리를 작성할 수 있습니다.

### 8. 운영 환경 최적화
- Prod 환경에서 보안 강화 (에러 정보 비노출)
- 응답 압축 및 HTTP/2 지원
- 최소한의 Actuator 엔드포인트만 노출

### 9. 커스텀 메트릭 및 멀티 인스턴스 지원
프로덕션 환경에서 여러 인스턴스가 동시에 실행될 때 각 인스턴스의 메트릭을 개별적으로 추적할 수 있습니다.

#### 메트릭 아키텍처
```
각 인스턴스 (In-Memory)
├── API 호출 카운터
├── 비즈니스 이벤트 카운터
├── 에러 카운터
└── 처리 시간 타이머
        ↓
   Prometheus (스크래핑)
        ↓
   Prometheus DB (시계열 저장)
        ↓
   Grafana (시각화)
```

#### 멀티 인스턴스 메트릭 수집
각 인스턴스는 자동으로 다음 태그를 메트릭에 추가합니다:
- `application`: 애플리케이션 이름
- `instance`: 인스턴스 ID (환경 변수 또는 자동 생성)
- `host`: 호스트명
- `port`: 포트 번호
- `environment`: 환경 (dev, prod 등)
- `region`: 리전 (선택적)

#### Kubernetes 배포 예시
`k8s/deployment.yml` 파일을 참고하세요:
- 3개의 레플리카로 실행
- Prometheus 자동 스크래핑 설정
- 각 Pod의 이름이 INSTANCE_ID로 주입
- Liveness/Readiness 프로브 설정

#### Prometheus 쿼리 예시
```promql
# 전체 인스턴스 합산
sum(api_calls_total{application="hoji"})

# 인스턴스별 조회
api_calls_total{application="hoji",instance=~".*"}

# 특정 인스턴스만
api_calls_total{application="hoji",instance="hoji-api-0"}

# 평균 응답 시간
rate(api_processing_time_sum[5m]) / rate(api_processing_time_count[5m])
```

#### 제공되는 메트릭
- `api.calls`: API 호출 횟수 (endpoint, method, status 태그)
- `business.events`: 비즈니스 이벤트 카운트
- `errors.count`: 에러 발생 횟수 (errorType, errorCode 태그)
- `processing.time`: 처리 시간 (operation 태그)

#### 사용 예시
```kotlin
@Service
class YourService(private val customMetrics: CustomMetrics) {
    fun processOrder(order: Order) {
        customMetrics.time("order.processing") {
            // 비즈니스 로직
            customMetrics.incrementBusinessEvent("order.created")
        }
    }
}
```

### 10. JPA Auditing
모든 엔티티에 자동으로 생성/수정 정보를 추적합니다:
```kotlin
@Entity
class YourEntity : BaseEntity() {
    // createdAt, updatedAt, createdBy, updatedBy는 자동 관리됨
}
```

- `createdAt`: 생성 시간
- `updatedAt`: 수정 시간
- `createdBy`: 생성자 (RequestContext의 userId 사용)
- `updatedBy`: 수정자 (RequestContext의 userId 사용)

### 11. 비동기 처리
`@Async` 어노테이션으로 비동기 작업 처리:
```kotlin
@Service
class NotificationService {
    @Async
    fun sendEmail(to: String, subject: String) {
        // 비동기로 실행됨
    }
}
```

ThreadPool 설정:
- Core Pool Size: 5
- Max Pool Size: 10
- Queue Capacity: 25
- Graceful Shutdown 지원

## 라이센스

MIT License
