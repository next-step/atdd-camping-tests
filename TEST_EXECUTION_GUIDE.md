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

### 테스트 구성 요소

```
src/test/
├── java/com/camping/tests/
│   ├── RunCucumberTest.java         # 테스트 진입점
│   └── steps/
│       └── PaymentSteps.java        # Step Definition
└── resources/features/
    ├── e2e.feature                  # E2E 테스트 시나리오
    ├── kiosk-smoke.feature          # 키오스크 스모크 테스트
    └── payment-e2e.feature          # 결제 E2E 테스트
```

## 🗂️ Gherkin 시나리오 예시

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

### IDE 설정

- **테스트 실행**: RunCucumberTest.java 실행
- **개별 시나리오**: feature 파일에서 시나리오별 실행 가능
- **디버깅**: Step Definition에 브레이크포인트 설정 가능