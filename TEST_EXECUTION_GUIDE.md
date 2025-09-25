# 테스트 실행 가이드

## 🎯 테스트 실행 순서

### 수동 단계별 실행

```bash
1. composeUp gradle task 실행

2. RunCucumberTest 실행
```

### 개별 서비스 관리

```bash
# 편의 명령어 (내부적으로 infraUp + appUp 실행)
./gradlew composeUp

# 서비스 종료 (내부적으로 appDown + infraDown 실행)
./gradlew composeDown
```

## 🔧 시스템 구성

### 서비스 포트 맵핑

| 서비스               | 컨테이너 포트 | 호스트 포트 | 설명          |
|-------------------|---------|--------|-------------|
| **Kiosk**         | 8080    | 18081  | 키오스크 서비스    |
| **Admin**         | 8080    | 18082  | 관리자 서비스     |
| **Reservation**   | 8080    | 18083  | 예약 서비스      |
| **Payments Mock** | 8080    | 18084  | 결제 Mock 서비스 |
| **MySQL DB**      | 3306    | 3306   | 데이터베이스      |

### 네트워크 구성

- **네트워크**: `atdd-net` (external)
- 모든 서비스가 동일한 Docker 네트워크에서 통신
- 서비스 간 내부 통신은 컨테이너명으로 접근 (예: `http://admin:8080`)

## 📁 프로젝트 구조

### Gradle 태스크 계층구조

```
smokeTest
├── composeUp
│   ├── infraUp    # DB 서비스 시작
│   └── appUp      # 앱 서비스 시작 (+ 소스 자동 클론)
├── test           # Cucumber 테스트 실행
└── composeDown    # 모든 서비스 종료
    ├── appDown
    └── infraDown
```

### 소스 코드 자동 관리

- `appUp` 실행 시 필요한 소스 저장소를 자동으로 클론:
    - `infra/repos/atdd-camping-kiosk/`
    - `infra/repos/atdd-camping-admin/`
    - `infra/repos/atdd-camping-reservation/`

## 🧪 테스트 기술 스택

### 테스트 프레임워크

- **Cucumber**: BDD 기반 시나리오 테스트
- **JUnit Platform**: 테스트 실행 플랫폼
- **REST Assured**: API 테스트 클라이언트
- **AssertJ**: 풍부한 어설션 라이브러리

### API 클라이언트 아키텍처

- **Factory 패턴**: 서비스별 클라이언트 생성 관리
- **Strategy 패턴**: HTTP 메서드별 요청 처리 전략
- **멀티 서비스 지원**: KIOSK(18081), ADMIN(18082), RESERVATION(18083)
- **자동 인증 관리**: 서비스별 JWT 토큰 자동 처리
- **ThreadLocal 격리**: 병렬 테스트 실행을 위한 컨텍스트 격리

### 테스트 구성 요소

```
src/test/
├── java/com/camping/tests/
│   ├── runner/
│   │   └── RunCucumberTest.java         # 테스트 진입점
│   ├── steps/                           # Step Definition 클래스들
│   │   ├── PaymentSteps.java           # 결제 관련 스텝
│   │   ├── IntegrationSteps.java       # 통합 테스트 스텝
│   │   └── Hooks.java                  # 테스트 전후 처리
│   └── support/                         # 테스트 지원 코드
│       ├── client/                      # API 클라이언트 시스템
│       │   ├── ApiClientFactory.java   # 서비스별 클라이언트 팩토리
│       │   └── impl/                    # 서비스별 구현체
│       ├── fixture/                     # 테스트 데이터 생성
│       │   ├── KioskTestFixture.java
│       │   └── PaymentTestFixture.java
│       └── helper/                      # 유틸리티 클래스
│           ├── ServiceType.java         # 서비스 타입 정의
│           └── ServiceContext.java      # 서비스 컨텍스트 관리
└── resources/features/
    ├── e2e.feature                      # E2E 테스트 시나리오
    ├── kiosk-smoke.feature              # 키오스크 스모크 테스트
    └── payment-e2e.feature              # 결제 E2E 테스트
```

## 🗂️ Gherkin 시나리오 및 API 클라이언트 활용 예시

### payment-e2e.feature

```gherkin
Feature: 키오스크 결제 E2E 테스트

  Scenario: 결제 성공 - 정상적인 금액으로 결제 요청
    Given 상품 목록에서 결제할 상품을 선택한다
      | productId | quantity | price |
      | 1         | 2        | 5000  |
    When 정상 금액으로 결제를 요청한다
    Then 결제가 성공한다
    And 결제 응답에 paymentKey가 포함되어 있다
    And 결제 응답에 orderId가 포함되어 있다
```

### API 클라이언트 사용 예시

```java
// 멀티 서비스 시나리오 예시
public class CrossServiceScenario {

    public void 예약_승인_워크플로우() {
        // 1단계: RESERVATION 서비스에서 예약 생성 (포트 18083)
        ExtractableResponse<Response> reservation = ApiClientFactory.reservation()
            .post("/api/reservations", reservationData);

        // 2단계: ADMIN 서비스에서 예약 승인 (포트 18082, 인증 필요)
        long reservationId = reservation.jsonPath().getLong("id");
        ApiClientFactory.admin()
            .patch("/api/admin/reservations/" + reservationId, approvalData, true);

        // 3단계: KIOSK에서 승인된 예약 확인 (포트 18081)
        ExtractableResponse<Response> confirmed = ApiClientFactory.kiosk()
            .get("/api/reservations/" + reservationId);
    }
}
```

## 🐳 Docker 설정

### 인프라 서비스 (docker-compose-infra.yml)

```yaml
services:
  db:
    image: mysql:8.0
    container_name: atdd-db
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=secret
      - MYSQL_DATABASE=atdd
```

### 애플리케이션 서비스 (docker-compose.yml)

- **빌드 방식**: Multi-stage Dockerfile
- **컨텍스트**: 프로젝트 루트 디렉토리
- **환경변수**: 각 서비스별 독립적인 H2 인메모리 DB 설정

### Dockerfile 구성 (Dockerfile-svc)

```dockerfile
# Builder Stage
FROM eclipse-temurin:17-jdk AS builder
ARG SERVICE_NAME  # 빌드 시 서비스명 전달
# 해당 서비스 소스 빌드

# Runtime Stage
FROM eclipse-temurin:17-jdk
# 빌드된 JAR 실행
```

## 🔍 트러블슈팅

### 일반적인 문제 해결

1. **포트 충돌**: 18081-18084, 3306 포트가 사용 중인지 확인
2. **Docker 데몬**: Docker Desktop이 실행 중인지 확인
3. **네트워크 충돌**: `docker network ls`로 atdd-net 확인
4. **소스 클론 실패**: Git SSH 키 설정 확인
5. **API 클라이언트 오류**: 잘못된 서비스 타입 사용 여부 확인
6. **인증 오류**: ServiceContext의 토큰 설정 상태 확인

### API 클라이언트 관련 문제

```bash
# 서비스별 상태 확인
curl http://localhost:18081/actuator/health  # KIOSK
curl http://localhost:18082/actuator/health  # ADMIN
curl http://localhost:18083/actuator/health  # RESERVATION

# 인증 토큰 테스트
curl -H "Authorization: Bearer <token>" http://localhost:18082/api/admin/test
```

### 로그 확인

```bash
# 서비스 로그 확인
docker logs atdd-kiosk
docker logs atdd-admin
docker logs atdd-reservation
docker logs payments-mock

# 모든 서비스 로그 실시간 확인
docker compose -f infra/docker-compose.yml logs -f
```

### 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# 서비스 상태 확인
curl http://localhost:18081/actuator/health
curl http://localhost:18082/actuator/health
curl http://localhost:18083/actuator/health
```

## ⚙️ 개발 환경 설정

### 필수 요구사항

- **Java 17+**
- **Docker & Docker Compose**
- **Git** (SSH 키 설정 필요)
- **IDE**: IntelliJ IDEA 또는 VS Code (Cucumber 플러그인 권장)

### IDE 설정

- **테스트 실행**: `src/test/java/com/camping/tests/runner/RunCucumberTest.java` 실행
- **개별 시나리오**: feature 파일에서 시나리오별 실행 가능
- **디버깅**: Step Definition에 브레이크포인트 설정 가능
- **API 클라이언트 디버깅**: ApiClientFactory와 BaseApiClient에 브레이크포인트 설정

### API 클라이언트 시스템 활용

```java
// 올바른 사용 예시
ApiClientFactory.kiosk().get("/api/products");              // 키오스크 상품 조회
ApiClientFactory.admin().get("/api/users", true);           // 관리자 사용자 목록 (인증)
ApiClientFactory.reservation().post("/api/reservations", data); // 예약 생성

// 피해야 할 사용법
// ❌ 직접 RestAssured 사용
// ❌ 하드코딩된 포트 사용
// ❌ 잘못된 서비스 타입 사용
```

## 🏗️ 테스트 아키텍처 개요

### 계층 구조

```
┌─────────────────┐
│   Cucumber      │  ← BDD 시나리오
│   Features      │
└─────────────────┘
          │
┌─────────────────┐
│   Step          │  ← 시나리오 구현
│   Definitions   │
└─────────────────┘
          │
┌─────────────────┐
│   Test          │  ← 테스트 데이터
│   Fixtures      │    생성 및 검증
└─────────────────┘
          │
┌─────────────────┐
│   API Client    │  ← HTTP 요청 처리
│   Factory       │
└─────────────────┘
          │
┌─────────────────┐
│   Service       │  ← 실제 마이크로
│   Endpoints     │    서비스들
└─────────────────┘
```

### 멀티 서비스 테스트 플로우

1. **테스트 초기화**: Hooks에서 모든 서비스 RequestSpec 설정
2. **인증 토큰 획득**: Admin 로그인 후 서비스별 토큰 설정
3. **시나리오 실행**: 적절한 서비스 클라이언트로 API 호출
4. **크로스 서비스 검증**: 여러 서비스 간 데이터 일관성 확인
5. **테스트 정리**: 각 시나리오 후 컨텍스트 초기화