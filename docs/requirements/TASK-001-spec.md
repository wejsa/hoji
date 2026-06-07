# TASK-001 인증/인가 시스템 (Spring Security + JWT)

## 개요

Spring Security와 JWT(JSON Web Token)를 기반으로 한 인증·인가 시스템을 구축한다.
현재 모든 API가 인증 없이 노출되어 있어, 회원가입·로그인·토큰 기반 접근 제어를 도입해 보안 기반을 마련한다.

| 항목 | 값 |
|------|-----|
| Task ID | TASK-001 |
| Phase | 1 (핵심 기능 / 보안 기반) |
| 우선순위 | High |
| 통신 방식 | REST (단방향 요청/응답) |
| 의존 Task | 없음 |

## 목적

- 인증되지 않은 외부 접근으로부터 API를 보호한다.
- 사용자별 권한(Role) 기반 접근 제어(RBAC)의 토대를 마련한다.
- 무상태(stateless) JWT 인증으로 수평 확장(멀티 인스턴스/Docker)에 적합한 구조를 갖춘다.

## 기능 요구사항

| ID | 설명 | 우선순위 | 수용 기준 |
|----|------|---------|----------|
| FR-001 | 회원가입 API (`POST /api/v1/auth/signup`) | High | username/email 중복 검증, 비밀번호 BCrypt 해싱 후 저장, 201 응답 |
| FR-002 | 로그인 API (`POST /api/v1/auth/login`) | High | 자격증명 검증 성공 시 Access Token + Refresh Token 발급, 실패 시 401 |
| FR-003 | Access Token 재발급 API (`POST /api/v1/auth/refresh`) | High | 유효한 Refresh Token으로 새 Access Token 발급, 만료/위조 시 401 |
| FR-004 | 로그아웃 API (`POST /api/v1/auth/logout`) | Medium | Refresh Token 무효화, 200 응답 |
| FR-005 | JWT 인증 필터 | High | 모든 보호 API 요청에서 Access Token 검증, SecurityContext에 인증 주입 |
| FR-006 | Role 기반 인가 (RBAC) | High | `USER`/`ADMIN` 역할 구분, 관리자 전용 엔드포인트 접근 제어(403) |
| FR-007 | User 도메인에 보안 필드 추가 | High | `password`(해시), `role` 컬럼 추가 및 마이그레이션 |
| FR-008 | 인증 사용자 정보 조회 (`GET /api/v1/auth/me`) | Medium | 토큰의 주체에 해당하는 현재 사용자 정보 반환 |
| FR-009 | 기존 User CRUD API 접근 제어 적용 | Medium | 인증 필요 엔드포인트 보호, 본인/관리자 권한 검증 |

## 비기능 요구사항

### 성능
- JWT 검증은 무상태(서명 검증)로 처리하여 요청당 DB 조회 최소화.
- Refresh Token 저장소는 로컬 캐시(Caffeine) 또는 DB 기반 (프로젝트 목표 스택상 Redis 미사용).

### 보안
- 비밀번호는 **BCrypt**로 해싱하여 저장하며 평문 로깅 금지.
- Access Token 만료 30분, Refresh Token 만료 14일 (설정값으로 분리).
- JWT 서명 시크릿은 환경변수/`application-*.yml` 외부 설정으로 관리(하드코딩 금지).
- 인증 실패 401 / 인가 실패 403을 기존 `ApiResponse` + `ResultCode`(UNAUTHORIZED/FORBIDDEN) 포맷으로 일관 반환.
- 로그인 brute-force 대비 향후 rate limiting 연계 여지 확보(본 Task 범위 외).

### 확장성
- 무상태 JWT로 멀티 인스턴스/Docker 환경 수평 확장 호환.
- Role enum 확장 가능한 구조(USER/ADMIN → 추가 역할 대응).

## 기술 스펙

### 영향 범위
- `domain/User.kt` — `password`, `role`(enum) 필드 추가
- `repository/UserRepository.kt` — 기존 `findByUsername` 활용
- `config/` — `SecurityConfig` 신규 (SecurityFilterChain, PasswordEncoder, AuthenticationManager)
- `controller/AuthController.kt` — 신규
- `service/AuthService.kt`, `service/JwtTokenProvider.kt` — 신규
- `common/exception` — 기존 `UnauthorizedException`/`ForbiddenException`/`ResultCode` 재사용
- `common/dto/ResultCode.kt` — 토큰 만료 등 코드 보강 가능
- `build.gradle.kts` — `spring-boot-starter-security`, `jjwt`(io.jsonwebtoken) 의존성 추가

### 의존성
- `org.springframework.boot:spring-boot-starter-security`
- `io.jsonwebtoken:jjwt-api / jjwt-impl / jjwt-jackson` (또는 동등 JWT 라이브러리)

### 통신 방식
- REST / JSON. 토큰은 `Authorization: Bearer <token>` 헤더로 전달.

### API 변경
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 | 불필요 |
| POST | `/api/v1/auth/login` | 로그인(토큰 발급) | 불필요 |
| POST | `/api/v1/auth/refresh` | Access Token 재발급 | Refresh Token |
| POST | `/api/v1/auth/logout` | 로그아웃 | 필요 |
| GET | `/api/v1/auth/me` | 현재 사용자 조회 | 필요 |
| `/api/v1/users/**` | 기존 CRUD | 접근 제어 적용 | 필요 |

## 테스트 계획

- **단위 테스트**: `JwtTokenProvider`(발급/검증/만료/위조), `AuthService`(가입 중복·로그인 성공/실패·재발급).
- **통합 테스트**: 회원가입→로그인→보호 API 접근 플로우, 토큰 없이 접근 시 401, 권한 부족 시 403, ADMIN 전용 엔드포인트 인가.
- **보안 테스트**: 비밀번호 해시 저장 확인, 만료/위조 토큰 거부.
- 목표 커버리지: 80% (프로젝트 컨벤션).

## 참고자료
- 기존 코드: `UserService`, `UserController`, `BusinessException`/`ResultCode`, `WebMvcConfig`/`CorsConfig`
- Spring Security 6.x (Spring Boot 3.2 기준), JJWT
- 프로젝트 목표 스택: 무상태 인증 + 로컬 캐시(Caffeine) + Docker
