# spring-boot-jwt-starter

## 프로젝트 개요

Kotlin + Spring Boot 기반 백엔드 서비스.

| 항목 | 값 |
|------|-----|
| **기술 스택** | Spring Boot 3.2 (Kotlin 1.9 / Java 17) |
| **빌드** | Gradle (Kotlin DSL) |
| **ORM** | JPA + QueryDSL |
| **데이터베이스** | H2 (인메모리) |
| **캐시** | 로컬 캐시 (Caffeine) |
| **이벤트** | Spring Application Event (메시지큐 미사용) |
| **인프라** | Docker (docker-compose) |
| **API 문서** | SpringDoc OpenAPI (Swagger) |
| **테스트** | JUnit5 + MockK + SpringMockK |

---

## 프레임워크 역할 경계

이 프로젝트는 **AI Crew Kit** 프레임워크 기반입니다.

| 프레임워크가 하는 것 | Claude가 하는 것 |
|---------------------|-----------------|
| 워크플로우 자동화 (plan→impl→review→merge) | 코드 작성 (모든 언어, 프로토콜, 패턴) |
| 품질 게이트 (빌드/테스트/리뷰 통과 필수) | 기술 판단 (아키텍처, 라이브러리 선택) |
| 팀 컨벤션 SSOT | 구현 지식 |

**원칙**: 프레임워크는 "어떤 프로세스로 만드는지"를 관리하고, Claude는 "어떻게 짜는지"를 담당합니다.

---

## 30초 요약 (Quick Reference)

| 하고 싶은 것 | 명령 |
|-------------|------|
| 새 기능 기획 | "새 기능 기획해줘" → `/crew-feature {기능명}` |
| 다음 작업 시작 | "다음 작업 가져와줘" → `/crew-plan` → `/crew-impl` |
| PR 리뷰 + 머지 | "PR N 리뷰해줘" → `/crew-review-pr` → `/crew-merge-pr` |
| 상태 확인 | `/crew-report` |

### 주의사항
1. **계획 승인 전 코드 작성 금지** — plan → 승인 → impl 순서 필수
2. **빌드/테스트 통과 필수** — PR 생성 전 자동 검증 (`./gradlew build`, `./gradlew test`)
3. **자동 체이닝 중 멈추지 않음** — impl → review → merge 자동 진행

---

## 에이전트 팀
- **PM** — 워크플로우 오케스트레이션
- **planner** — 요구사항/기획 문서 구조화
- **backend** — 구현 (Kotlin / Spring Boot)
- **db-designer** — DB 설계 분석
- **code-reviewer** — 다관점 코드 리뷰
- **qa** — 테스트 품질 분석
- **docs** — API 문서 / README / 아키텍처 문서

---

## 워크플로우
plan → impl → review → merge. 자세한 스킬은 `/crew-*` 명령으로 호출합니다.

## 빌드 / 테스트
```bash
./gradlew build   # 빌드
./gradlew test    # 테스트
```

## 컨벤션
- Task 접두사: `TASK-` (예: `TASK-001`)
- 브랜치 전략: git-flow
- 커밋 포맷: conventional commits
- PR 라인 제한: 500
- 테스트 커버리지 목표: 80%

<!-- CUSTOM_SECTION:START -->
<!-- 프로젝트별 커스텀 컨벤션을 이 영역에 추가하세요. crew-upgrade가 이 영역을 보존합니다. -->
<!-- CUSTOM_SECTION:END -->
