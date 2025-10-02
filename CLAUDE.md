# CLAUDE.md

이 파일은 이 저장소의 코드 작업 시 Claude Code (claude.ai/code)에게 가이드를 제공합니다.

## 📚 필수 문서

테스트 작업 시 다음 문서를 **반드시** 참조하세요:

- **`docs/acceptance-test-guide.md`**: 테스트 코드 작성 가이드 (Feature, Step Definition, Helper 작성법)
- **`docs/auth-setup.md`**: 환경 설정 및 실행 가이드 (인증, 데이터 시딩, 트러블슈팅)

## 프로젝트 개요

Cucumber/BDD를 사용한 캠핑 예약 시스템의 다중 서비스 인수 테스트 프레임워크입니다. 프로젝트는 Docker Compose를 통해 세 가지 주요 Spring Boot 서비스(kiosk, admin, reservation)와 외부 의존성(MySQL 데이터베이스, WireMock 결제 모킹)을 조율하며, Cucumber(Gherkin)로 작성된 테스트를 RestAssured를 사용하여 실행합니다.

## 아키텍처

### 서비스 구조
- **kiosk** (port 8081): 고객용 키오스크 서비스
- **admin** (port 8082): 관리자 서비스
- **reservation** (port 8083): 예약 관리 서비스
- **payments-mock** (port 8084): WireMock 기반 결제 게이트웨이 모킹
- **atdd-db** (port 3306): 공유 MySQL 데이터베이스

서비스는 별도의 GitHub 저장소에서 `repos/` 디렉터리로 클론되고 Docker 컨테이너로 빌드됩니다.

### 테스트 프레임워크
- **Features**: `src/test/resources/features/`의 Cucumber `.feature` 파일
- **Step Definitions**: `src/test/java/com/camping/tests/steps/`의 Java 스텝 구현
- **API Helpers**: `src/test/java/com/camping/tests/helpers/`의 RestAssured 래퍼
- **Context**: 스텝 간 데이터 공유를 위한 스레드 안전 테스트 컨텍스트 (`CommonContext`)
- **Hooks**: `Hooks.java`의 설정/정리를 위한 Cucumber 라이프사이클 훅

## 주요 커맨드

### 초기 설정
```bash
# 세 가지 서비스 저장소 클론 (kiosk, admin, reservation)
./gradlew SetupAllServices
```

### Docker Compose 관리
```bash
# 모든 서비스 시작 (인프라 + 애플리케이션)
./gradlew ComposeAllUp

# 모든 서비스 중지
./gradlew ComposeAllDown
```

### 테스트
```bash
# 모든 테스트 실행
./gradlew test

# 특정 기능 테스트 실행
./gradlew test --tests "com.camping.tests.RunCucumberTest" -Dcucumber.filter.tags="@tagname"
```

### WireMock 검증
```bash
# WireMock 스텁이 로드되었는지 확인
./gradlew verifyWiremockStubs
```

## 주요 구현 세부사항

### 서비스 URL (시스템 속성)
Base URL은 `build.gradle.kts`에서 시스템 속성으로 구성됩니다:
- `KIOSK_BASE_URL`: http://localhost:8081
- `ADMIN_BASE_URL`: http://localhost:8082
- `RESERVATION_BASE_URL`: http://localhost:8083
- `PAYMENTS_BASE_URL`: http://localhost:8084

### WireMock 스텁
결제 게이트웨이 스텁은 `infra/wiremock/mappings/`에 있으며 볼륨 마운트를 통해 자동으로 로드됩니다. 스텁은 `request` (method, urlPath)와 `response` (status, body) 섹션이 포함된 WireMock JSON 형식을 따라야 합니다.

### 저장소 관리
`repo-setup.gradle.kts` 태스크는 GitHub에서 서비스를 클론하고 `parkSeryu` 브랜치를 체크아웃합니다. 서비스 저장소:
- kiosk: https://github.com/ParkSeryu/atdd-camping-kiosk.git
- admin: https://github.com/ParkSeryu/atdd-camping-admin.git
- reservation: https://github.com/ParkSeryu/atdd-camping-reservation.git

### Docker 빌드 프로세스
서비스는 공유 Dockerfile (`infra/dockerfiles/Dockerfile`)을 사용하여 빌드되며, `SERVICE_NAME` 빌드 인자를 사용하여 `repos/` 디렉터리에서 빌드할 서비스를 선택합니다.

### 테스트 컨텍스트 관리
`CommonContextHolder`는 다음을 유지하는 ThreadLocal 싱글톤입니다:
- RestAssured용 RequestSpecification
- Admin 인증 토큰
- 마지막 API 응답

이 컨텍스트는 `@Before` 훅에서 초기화되고 `@After` 훅에서 정리됩니다.

### 서비스 의존성
kiosk 서비스는 admin 및 payments-mock이 정상 상태여야 합니다 (`docker-compose.yml` healthcheck 참조). 모든 애플리케이션 서비스는 환경 변수를 통해 주입된 연결 정보로 동일한 MySQL 데이터베이스를 공유합니다.
