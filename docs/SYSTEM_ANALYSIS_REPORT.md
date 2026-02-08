# System Analysis Report

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트명 | atdd-tests |
| 목적 | 캠핑 키오스크 애플리케이션에 대한 ATDD(인수 테스트 주도 개발) 테스트 프로젝트 |
| Java 버전 | 17 |
| Gradle 버전 | 8.14 |
| 빌드 스크립트 | build.gradle.kts (Kotlin DSL) |

이 프로젝트는 외부 저장소(`atdd-camping-kiosk`)의 키오스크 애플리케이션을 Docker로 기동한 뒤,
Cucumber BDD 기반의 인수 테스트를 실행하는 구조이다.

---

## 2. 디렉토리 구조

```
atdd-camping-tests/
├── build.gradle.kts              # 테스트 프로젝트 빌드 설정
├── settings.gradle               # rootProject.name = 'atdd-tests'
├── gradlew / gradlew.bat         # Gradle Wrapper
├── docs/                         # 미션 문서
│   ├── 미션요구사항.md
│   └── 미션진행_command.txt
├── dockerfiles/                  # Dockerfile 모음
│   ├── Dockerfile                    # 범용 Spring Boot Dockerfile
│   └── Dockerfile-kiosk              # 키오스크 전용 멀티스테이지 빌드
├── infra/                        # 인프라 구성
│   ├── docker-compose.yml            # 키오스크 앱 컨테이너
│   ├── docker-compose-infra.yml      # MySQL 등 인프라 컨테이너
│   ├── db/
│   │   └── init.sql                  # DB 초기 데이터 (상품, 캠핑사이트, 예약 등)
│   └── wiremock/
│       └── mappings/
│           └── payment-approve.json  # 결제 API 목(Mock) 응답
├── repos/                        # 테스트 대상 애플리케이션 (git clone)
│   └── atdd-camping-kiosk/           # 캠핑 키오스크 Spring Boot 앱
└── src/test/                     # 테스트 코드
    ├── java/com/camping/tests/
    │   ├── RunCucumberTest.java       # Cucumber 테스트 러너
    │   └── steps/
    │       └── SampleSteps.java       # 샘플 Step Definition
    └── resources/features/
        └── sample.feature             # 샘플 시나리오 (헬스 체크)
```

---

## 3. 기술 스택

### 3.1 테스트 프로젝트 (atdd-tests)

| 분류 | 기술 | 버전 |
|------|------|------|
| BDD 프레임워크 | Cucumber | 7.14.0 |
| 테스트 러너 | JUnit 5 (Jupiter) | 5.10.0 |
| API 테스트 | RestAssured | 5.3.2 |
| JSON 처리 | Jackson Databind | 2.17.2 |
| DB 연동 | MySQL Connector/J | 8.3.0 |

### 3.2 키오스크 애플리케이션 (atdd-camping-kiosk)

| 분류 | 기술 | 버전 |
|------|------|------|
| 프레임워크 | Spring Boot | 3.2.0 |
| 뷰 템플릿 | Thymeleaf | (Spring Boot 관리) |
| 재시도 | Spring Retry | 2.0.5 |
| 코드 생성 | Lombok | (Spring Boot 관리) |
| 테스트 목 | WireMock | 2.35.0 |

### 3.3 인프라

| 분류 | 기술 |
|------|------|
| 컨테이너 | Docker Compose |
| 데이터베이스 | MySQL 8.0 |
| JDK 이미지 | Eclipse Temurin 17 |
| API 목 서버 | WireMock (정적 매핑) |

---

## 4. 테스트 대상 애플리케이션 분석 (atdd-camping-kiosk)

### 4.1 애플리케이션 설정

- 포트: `8080`
- DB 미사용: DataSource, JPA 자동 설정 비활성화
- 외부 연동: Admin 서비스, Payment 게이트웨이

```yaml
# 주요 설정 (application.yml)
kiosk:
  admin:
    base-url: http://localhost:8080
    auth: { username: admin, password: admin123 }
  payment:
    base-url: http://localhost:9090
    secret-key: ${PAYMENTS_SECRET_KEY:test_sk_dummy}
  discount:
    percent: 10
```

### 4.2 아키텍처 다이어그램

```
[브라우저] ──── [키오스크 앱 :8080] ──┬── [Admin 서비스]   상품 조회, 판매 확정
                                      └── [Payment 서비스]  결제 생성/승인/환불
```

### 4.3 패키지 구조 및 클래스

```
com.camping.kiosk
├── KioskApplication.java              # @SpringBootApplication, @EnableRetry
├── config/
│   ├── RequestIdFilter.java           # X-Request-Id 헤더 → MDC 전파 필터
│   └── RestClientConfig.java          # RestTemplate 설정 (타임아웃, 인터셉터)
├── domain/
│   └── CartItem.java                  # 장바구니 아이템 (productId, name, price, qty)
├── external/
│   ├── admin/
│   │   ├── AdminAuthClient.java       # Admin 인증 (쿠키 기반, 토큰 캐싱)
│   │   ├── AdminClient.java           # Admin API (상품 목록, 판매 확정)
│   │   ├── Product.java               # 상품 DTO
│   │   ├── SaleItem.java              # 판매 아이템 DTO
│   │   └── SaleRequest.java           # 판매 요청 DTO
│   └── payment/
│       ├── PaymentClient.java         # 결제 API (생성/승인/환불, Basic Auth)
│       └── dto/
│           ├── PaymentCreateRequest.java
│           ├── PaymentCreateResult.java
│           ├── PaymentConfirmRequest.java
│           └── PaymentConfirmResponse.java
├── service/
│   ├── AdminService.java              # Admin 위임 서비스
│   └── PaymentService.java            # 결제 오케스트레이션 (재시도 포함)
└── web/
    ├── HomeController.java            # GET / (메인 페이지), GET /health
    ├── PaymentController.java         # POST /api/payments, /api/payments/confirm
    └── ProductController.java         # GET /api/products
```

### 4.4 주요 API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/health` | 헬스 체크 ("OK" 반환) |
| GET | `/` | 메인 페이지 (상품 목록 + 장바구니 UI) |
| GET | `/api/products` | 상품 목록 조회 (Admin 서비스 위임) |
| POST | `/api/payments` | 결제 생성 (금액 계산 → Payment 서비스 호출) |
| POST | `/api/payments/confirm` | 결제 승인 (Payment 승인 → Admin 판매 확정) |

### 4.5 핵심 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|------|-----------|------|
| Retry | `PaymentService.confirmSale()` | `@Retryable` 3회 재시도, 200ms 백오프 |
| Token Caching | `AdminAuthClient` | 인증 토큰을 캐싱하고 만료 시 재인증 |
| Request ID Propagation | `RequestIdFilter` + `RestClientConfig` | X-Request-Id를 MDC에 저장, 외부 호출 시 전파 |
| Two-Phase Payment | `PaymentService` | 결제 생성(create) → 결제 승인(confirm) 2단계 처리 |

---

## 5. 인프라 구성 분석

### 5.1 Docker Compose 서비스

| 파일 | 서비스 | 이미지 | 포트 | 용도 |
|------|--------|--------|------|------|
| docker-compose.yml | kiosk | 로컬 빌드 (Dockerfile-kiosk) | 18081→8080 | 키오스크 앱 |
| docker-compose-infra.yml | db | mysql:8.0 | 3306→3306 | MySQL DB |

### 5.2 Dockerfile-kiosk (멀티스테이지 빌드)

```
Stage 1 (build)                          Stage 2 (run)
┌──────────────────────────┐            ┌──────────────────────────┐
│ eclipse-temurin:17-jdk   │            │ eclipse-temurin:17-jdk   │
│                          │            │                          │
│ COPY 소스코드            │  JAR 복사  │ COPY --from=build *.jar  │
│ gradlew bootJar -x test ─┼───────────►│ java -jar app.jar       │
└──────────────────────────┘            └──────────────────────────┘
```

### 5.3 DB 초기 데이터 (init.sql)

| 테이블 | 레코드 수 | 설명 |
|--------|-----------|------|
| products | 12 | 렌탈 장비 + 판매 상품 |
| campsites | 35 | A-1~A-20, B-1~B-15 구역 |
| reservations | 17 | 다양한 날짜의 예약 데이터 |
| sales_records | 5 | 판매 기록 |
| rental_records | 6 | 대여 기록 (워크인 포함) |

### 5.4 WireMock 매핑

| 엔드포인트 | 응답 | 용도 |
|-----------|------|------|
| POST `/v1/payments` | `{ paymentKey, orderId, status: "APPROVED" }` | 결제 승인 목 |

---

## 6. 테스트 구조 분석

### 6.1 테스트 실행 흐름

```
./gradlew test
    └── RunCucumberTest (@Suite)
            └── features/*.feature 로드
                    └── SampleSteps.java (Step Definition 매핑)
```

### 6.2 현재 테스트 현황

| 구분 | 상태 |
|------|------|
| Feature 파일 | 1개 (sample.feature) |
| Step Definition | 1개 (SampleSteps.java) |
| 시나리오 | 헬스 체크 (GET localhost:8080 → 성공 확인) |

현재는 샘플 수준의 테스트만 존재하며, 실제 API 호출 검증이나 DB 상태 확인 등의 인수 테스트는 아직 작성되지 않은 상태이다.

---

## 7. Gradle Task

| Task | 그룹 | 명령어 | 설명 |
|------|------|--------|------|
| `composeUp` | infra | `./gradlew composeUp` | 키오스크 컨테이너 빌드 및 기동 |
| `composeDown` | infra | `./gradlew composeDown` | 키오스크 컨테이너 종료 및 볼륨 삭제 |
| `test` | verification | `./gradlew test` | Cucumber 인수 테스트 실행 |

---

## 8. 외부 의존 관계

```
                    ┌─────────────────────┐
                    │   atdd-tests        │
                    │   (테스트 프로젝트)    │
                    └────────┬────────────┘
                             │ Cucumber + RestAssured
                             ▼
                    ┌─────────────────────┐
 ┌─────────────────►│   kiosk :18081      │◄─────────────────┐
 │  상품 조회/판매확정 │   (Docker 컨테이너)  │ 결제 생성/승인/환불 │
 │                  └─────────────────────┘                  │
 │                                                           │
 ▼                                                           ▼
┌──────────────┐                                 ┌──────────────────┐
│ Admin 서비스  │                                 │ Payment 서비스    │
│ (미구성)      │                                 │ (WireMock :9090) │
└──────────────┘                                 └──────────────────┘
         │
         ▼
┌──────────────┐
│ MySQL :3306  │
│ (atdd-infra) │
└──────────────┘
```

---

## 9. Docker Compose 실행 가이드

### 9.1 사전 준비

- Docker Desktop이 설치되어 있고 실행 중이어야 한다.
- `repos/atdd-camping-kiosk/` 디렉토리에 키오스크 소스코드가 clone 되어 있어야 한다.

### 9.2 인프라 (MySQL) 실행/종료

```bash
# MySQL 컨테이너 기동
docker compose -f infra/docker-compose-infra.yml up -d

# MySQL 컨테이너 종료 및 볼륨 삭제
docker compose -f infra/docker-compose-infra.yml down -v
```

| 항목 | 값 |
|------|-----|
| 컨테이너명 | atdd-db |
| 포트 | 3306 |
| DB명 | atdd |
| Root 비밀번호 | secret |

### 9.3 키오스크 앱 실행/종료

```bash
# Gradle Task 사용 (권장)
./gradlew composeUp      # 빌드 + 컨테이너 기동
./gradlew composeDown     # 컨테이너 종료 + 볼륨 삭제

# docker compose 직접 사용
docker compose -f infra/docker-compose.yml up -d --build   # 빌드 + 기동
docker compose -f infra/docker-compose.yml down -v          # 종료 + 볼륨 삭제
```

| 항목 | 값 |
|------|-----|
| 포트 | 18081 (호스트) → 8080 (컨테이너) |
| 프로필 | local |
| 헬스 체크 | `http://localhost:18081/health` |

### 9.4 전체 실행 순서 (인프라 + 앱 + 테스트)

```bash
# 1. 인프라 기동
docker compose -f infra/docker-compose-infra.yml up -d

# 2. 키오스크 앱 기동
./gradlew composeUp

# 3. 앱 기동 확인 (헬스 체크)
curl http://localhost:18081/health

# 4. 인수 테스트 실행
./gradlew test

# 5. 정리 (역순)
./gradlew composeDown
docker compose -f infra/docker-compose-infra.yml down -v
```

### 9.5 로그 확인 / 트러블슈팅

```bash
# 키오스크 앱 로그 확인
docker compose -f infra/docker-compose.yml logs -f kiosk

# 인프라 로그 확인
docker compose -f infra/docker-compose-infra.yml logs -f db

# 컨테이너 상태 확인
docker ps

# 키오스크 앱 강제 재빌드 (캐시 무시)
docker compose -f infra/docker-compose.yml build --no-cache
docker compose -f infra/docker-compose.yml up -d
```

---

## 10. 식별된 특이사항

1. **Admin 서비스 미연동**: `kiosk.admin.base-url`이 `localhost:8080`(자기 자신)을 가리키고 있어, Admin 서비스와의 실제 연동이 구성되지 않은 상태이다.
2. **DB 미사용**: 키오스크 앱은 DataSource/JPA 자동 설정을 비활성화하고 있다. DB는 인프라 compose에 정의되어 있으나 키오스크 앱과 직접 연결되지 않는다.
3. **WireMock 미기동**: `payment-approve.json` 매핑 파일은 존재하지만, WireMock 컨테이너가 docker-compose에 정의되어 있지 않다.
4. **네트워크 분리**: `docker-compose.yml`(kiosk)과 `docker-compose-infra.yml`(db)이 별도 파일로 분리되어 있으며, 동일 네트워크에 대한 명시적 연결이 없다.
5. **테스트 미구현**: 현재 샘플 시나리오만 존재하며, 실제 비즈니스 시나리오(상품 조회, 결제, 환불 등)에 대한 인수 테스트가 작성되지 않았다.
