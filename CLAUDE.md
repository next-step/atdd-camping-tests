# CLAUDE.md

이 파일은 AI(Claude Code)가 테스트 코드를 작성할 때 참조하는 프로젝트 가이드입니다.

## 📚 필수 문서

**테스트 작업 전 반드시 해당 문서를 읽으세요:**

- **`docs/acceptance-test-guide.md`**: 테스트 코드 작성 가이드 (Feature, Step Definition, Helper 패턴)
- **`docs/auth-setup.md`**: 환경 설정 및 실행 가이드 (Docker, 인증, 데이터 시딩, 트러블슈팅)

## 프로젝트 개요

캠핑 예약 시스템의 다중 서비스 E2E 테스트 프레임워크 (Cucumber + RestAssured + Docker Compose)

### 서비스 구조
```
kiosk (8081) ──> admin (8082)
             ──> reservation (8083)
             ──> payments-mock (8084) [WireMock]

All services ──> atdd-db (3306) [MySQL]
```

### 테스트 플로우
```
.feature → Step Definitions → API Helpers → RestAssured → Docker Services
```

## 빠른 시작

```bash
# 1. 서비스 저장소 클론 (최초 1회)
./gradlew SetupAllServices

# 2. 모든 서비스 시작
./gradlew ComposeAllUp

# 3. 테스트 실행
./gradlew test

# 4. 환경 종료
./gradlew ComposeAllDown
```

## 핵심 규칙

### 코드 작성 시
1. **API 호출은 반드시 Helper 클래스 사용** (Step에서 직접 호출 금지)
2. **Response는 반드시 `CommonContextHolder`에 저장** (스텝 간 데이터 공유)
3. **상태 코드 + 비즈니스 로직 모두 검증** (AssertJ 사용)
4. **Base URL은 System Property 사용** (`KIOSK_BASE_URL` 등)
5. **Gherkin은 영어 키워드(Given/When/Then) + 한국어 스텝 내용 사용** (프로젝트 표준)

### 인증
- `@Before` 훅에서 자동으로 admin 토큰 초기화됨 (수동 로그인 불필요)
- Helper에서 `CommonContextHolder.getInstance().getAdminToken()` 사용
- 인증 필요 API는 `.cookie("AUTH_TOKEN", token)` 추가

### 데이터 관리
- DB 초기 데이터는 `infra/db/init.sql`에서 자동 시딩됨
- 테스트 데이터는 Feature에서 파라미터로 전달
- WireMock 스텁은 `infra/wiremock/mappings/`에 JSON 파일로 추가

## 상세 가이드

### 코드 작성법
→ `docs/acceptance-test-guide.md` 참조
- Step Definition 작성 패턴
- API Helper 작성 패턴
- Feature 파일 작성법
- WireMock 스텁 작성법
- 좋은/나쁜 예시

### 환경 설정
→ `docs/auth-setup.md` 참조
- Docker Compose 실행 흐름
- 인증 시스템 동작 방식
- 데이터 시딩 메커니즘
- 트러블슈팅 가이드

## 저장소 정보

서비스는 별도 저장소에서 `repos/` 디렉터리로 클론됩니다:
- kiosk: https://github.com/ParkSeryu/atdd-camping-kiosk.git
- admin: https://github.com/ParkSeryu/atdd-camping-admin.git
- reservation: https://github.com/ParkSeryu/atdd-camping-reservation.git

Branch: `parkSeryu`
