# Step 03: WireMock을 활용한 결제 E2E 테스트

## 목표

외부 결제 서비스를 WireMock으로 모킹하여 E2E 테스트 구현

## 실행 순서

### Gradle 태스크 (권장)

| 태스크 | 설명 |
|--------|------|
| `./gradlew infraUp` | ① 인프라(DB) 기동 |
| `./gradlew servicesUp` | ② 앱 + WireMock 기동 |
| `./gradlew test` | ④ E2E 테스트 실행 |
| `./gradlew servicesDown` | 앱 종료 |
| `./gradlew infraDown` | DB 종료 + 볼륨 삭제 |
| `./gradlew allUp` | ①+② 한번에 기동 |
| `./gradlew allDown` | ⑤ 전체 종료 |

```bash
# 전체 실행
./gradlew allUp
./gradlew test
./gradlew allDown

# 또는 단계별 실행
./gradlew infraUp      # ① DB
./gradlew servicesUp   # ② 앱 + WireMock
./gradlew test         # ④ 테스트
./gradlew allDown      # ⑤ 정리
```

> ③ WireMock 스텁은 `infra/wiremock/mappings/`에서 자동 로드되므로 별도 작업 불필요

### Docker Compose 직접 실행

```bash
# ① 인프라(DB) 기동
docker compose -f infra/docker-compose-infra.yml down -v
docker compose -f infra/docker-compose-infra.yml up -d --wait

# ② 앱 + WireMock(payments) 기동
docker compose -f infra/docker-compose.yml up -d --build

# ③ WireMock 스텁 확인
curl http://localhost:19090/__admin/mappings

# ④ E2E 테스트 실행
./gradlew test

# ⑤ 정리
./gradlew allDown
```

## 서비스 구성

| 서비스 | 호스트 포트 | 외부 URL (테스트 코드) | 내부 URL (Docker 서비스 간) |
|--------|------------|----------------------|--------------------------|
| admin | 18080 | `http://localhost:18080` | `http://admin:8080` |
| kiosk | 18081 | `http://localhost:18081` | `http://kiosk:8080` |
| reservation | 18082 | `http://localhost:18082` | `http://reservation:8080` |
| payments (WireMock) | 19090 | `http://localhost:19090` | `http://payments:8080` |
| atdd-db (MySQL) | 3306 | `jdbc:mysql://localhost:3306` | `jdbc:mysql://atdd-db:3306` |

> **참고**: 모든 서비스의 컨테이너 내부 포트는 `8080`이며, 호스트 포트만 다릅니다.
> 테스트 코드(호스트)에서는 `localhost:{호스트포트}`, Docker 컨테이너 간에는 `{서비스명}:8080`을 사용합니다.

## WireMock 구성

### 실행 방식
- Docker Compose 서비스로 실행
- `atdd-net` 네트워크에 합류
- Kiosk → `http://payments:8080` (내부 통신)

### 스텁 관리
정적 매핑 파일로 자동 로드:
```
infra/wiremock/
├── mappings/
│   ├── payment-create.json          # 결제 생성 (성공)
│   ├── payment-confirm-success.json # 결제 확정 성공 (priority: 10)
│   └── payment-confirm-failure.json # 결제 확정 실패 (priority: 1)
└── __files/                         # 응답 파일 (필요시)
```

### 스텁 우선순위
- Priority 1: `amount == 12345` → 400 에러 (결제 실패)
- Priority 10: 그 외 → 200 성공 (결제 승인)

## 환경변수 외부화

### 설정 우선순위 (TestConfig)

```
1. 환경변수        ADMIN_BASE_URL, KIOSK_BASE_URL, PAYMENTS_BASE_URL
2. 시스템 프로퍼티  -Dadmin.base-url=...
3. test.properties  admin.base-url=...
```

| 환경변수 | 프로퍼티 키 | 기본값 |
|---------|------------|--------|
| `ADMIN_BASE_URL` | `admin.base-url` | http://localhost:18080 |
| `KIOSK_BASE_URL` | `kiosk.base-url` | http://localhost:18081 |
| `RESERVATION_BASE_URL` | `reservation.base-url` | http://localhost:18082 |
| `PAYMENTS_BASE_URL` | `payments.base-url` | http://localhost:19090 |

### CI 환경에서 실행

```bash
ADMIN_BASE_URL=http://admin:8080 \
KIOSK_BASE_URL=http://kiosk:8080 \
./gradlew test
```

### Docker Compose (kiosk → 내부 서비스)
```yaml
environment:
  - KIOSK_PAYMENT_BASE_URL=http://payments:8080
  - KIOSK_PAYMENT_SECRET_KEY=test_sk_dummy
```

## 테스트 시나리오

### 결제 Happy Path (kiosk → WireMock)
```gherkin
시나리오: 결제 승인 성공
  조건 Admin에 다음 상품이 등록되어 있다
    | name | price |
    | 텐트  | 50000 |
  만약 키오스크에 결제 생성을 요청한다
  그리고 키오스크에 결제 확정을 요청한다
  그러면 결제가 성공이어야 한다
```

### 결제 Sad Path (kiosk → WireMock)
```gherkin
시나리오: 결제 승인 실패
  조건 Admin에 다음 상품이 등록되어 있다
    | name | price |
    | 텐트  | 50000 |
  만약 키오스크에 결제 생성을 요청한다
  그리고 키오스크에 금액 12345원으로 결제 확정을 요청한다
  그러면 결제가 실패이어야 한다
```

## 코드 구조

### DTO
```
dto/
├── LoginRequest.java
├── ProductRequest.java
├── CartItem.java
├── PaymentCreateRequest.java
└── PaymentConfirmRequest.java
```

### Support (API Client)
```
support/
├── ApiClient.java      # 범용 HTTP 클라이언트
├── AdminClient.java    # Admin API (로그인 자동화)
├── KioskClient.java    # Kiosk API (상품, 결제)
└── Endpoints.java      # API 경로 상수
```

## 완료 기준 체크리스트

- [x] WireMock 서비스가 `atdd-net`에 합류
- [x] 포트 충돌 없음 (19090)
- [x] 결제 베이스 URL 외부화 (`KIOSK_PAYMENT_BASE_URL`)
- [x] 성공/실패 경로 테스트 커버
- [x] 실행 방법 문서화