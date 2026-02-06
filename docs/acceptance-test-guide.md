# 인수 테스트 작성 가이드

AI 도구를 활용하여 시스템 레벨 인수 테스트를 생성하기 위한 컨텍스트 문서입니다.

## 시스템 개요

캠핑 예약 시스템으로 다음 서비스로 구성됩니다:

```
┌─────────┐     ┌─────────────┐     ┌─────────┐
│  Kiosk  │────▶│ Reservation │────▶│  Admin  │
└────┬────┘     └─────────────┘     └─────────┘
     │                                   │
     │          ┌──────────────┐         │
     └─────────▶│   Payments   │         │
                │  (WireMock)  │         │
                └──────────────┘         │
                                         ▼
                                    ┌─────────┐
                                    │   DB    │
                                    │ (MySQL) │
                                    └─────────┘
```

| 서비스 | 역할 | 호스트 포트 | 외부 URL (테스트 코드) | 내부 URL (Docker 서비스 간) |
|--------|------|------------|----------------------|--------------------------|
| Kiosk | 고객용 키오스크 (상품 조회, 결제) | 18081 | `http://localhost:18081` | `http://kiosk:8080` |
| Admin | 관리자 서비스 (상품/예약 관리) | 18080 | `http://localhost:18080` | `http://admin:8080` |
| Reservation | 예약 서비스 | 18082 | `http://localhost:18082` | `http://reservation:8080` |
| Payments (WireMock) | 외부 결제 서비스 Mock | 19090 | `http://localhost:19090` | `http://payments:8080` |

> **외부 vs 내부 URL 구분**
>
> - **외부 URL** (`localhost:{호스트포트}`): 호스트 머신에서 실행되는 **테스트 코드**가 사용. `test.properties`에 정의.
> - **내부 URL** (`{서비스명}:8080`): Docker 네트워크(`atdd-net`) 안에서 **컨테이너 간 통신**에 사용. `docker-compose.yml` 환경변수에 정의.
>
> 예) Kiosk 컨테이너가 결제 요청 시 → `http://payments:8080` (내부)
> 예) 테스트 코드에서 WireMock 상태 확인 시 → `http://localhost:19090` (외부)

## 엔드포인트 요약

### Admin API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/auth/login` | 로그인 (JWT 발급) | 불필요 |
| GET | `/admin/products` | 상품 목록 조회 | JWT |
| POST | `/admin/products` | 상품 등록 | JWT |
| GET | `/admin/reservations` | 예약 목록 조회 | JWT |
| PATCH | `/admin/reservations/{id}` | 예약 상태 변경 | JWT |

### Kiosk API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/products` | 상품 목록 조회 (Admin에서 조회) |
| POST | `/api/payments` | 결제 생성 |
| POST | `/api/payments/confirm` | 결제 확정 |

### Reservation API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/reservations` | 예약 생성 |
| GET | `/api/reservations/{id}` | 예약 조회 |
| POST | `/api/reservations/{id}/cancel` | 예약 취소 |

### Payments (WireMock)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/payments/confirm` | 결제 승인 |
| GET | `/__admin/mappings` | 스텁 목록 조회 |
| GET | `/__admin/requests` | 호출 기록 조회 |

## 사용자 여정

### 1. 정상 예약 흐름
```
고객이 키오스크에서 상품 선택
  → 결제 생성 요청 (Kiosk)
  → 결제 확정 요청 (Kiosk → Payments)
  → 결제 승인 (Payments 200)
  → 예약 확정 (Reservation CONFIRMED)
  → Admin에 반영 (PAID)
```

### 2. 결제 실패 흐름
```
고객이 키오스크에서 결제 시도
  → 결제 확정 요청
  → 결제 거절 (Payments 4xx/5xx)
  → 예약 실패/미확정
  → 재고 복원
```

### 3. 예약 취소 흐름
```
고객이 예약 취소 요청
  → 환불 처리 (Payments)
  → 예약 상태 변경 (CANCELLED)
  → Admin 반영
  → 재고 복원
```

## 인증 규칙

### Admin 인증
- 방식: JWT Bearer Token
- 토큰 획득: `POST /auth/login` 호출
- 헤더: `Authorization: Bearer {token}`
- 자격 증명: 환경변수로 외부화 (`ADMIN_USERNAME`, `ADMIN_PASSWORD`)

### Kiosk/Reservation
- 인증 불필요 (내부 서비스 간 통신)

## 시드 데이터 규칙

### 테스트 데이터 생성
- 각 시나리오에서 필요한 데이터는 `조건` 스텝에서 직접 생성
- 고유 식별자 사용 권장 (타임스탬프, UUID 접미사)

### 데이터 격리
- 시나리오 간 데이터 간섭 방지
- 테스트 후 정리 불필요 (재실행 안전)

## 환경 설정 규칙

### 필수 외부화 항목
```properties
# Base URLs
admin.base-url=http://localhost:18080
kiosk.base-url=http://localhost:18081
reservation.base-url=http://localhost:18082
payments.base-url=http://localhost:19090

# Credentials (하드코딩 금지)
admin.username=${ADMIN_USERNAME}
admin.password=${ADMIN_PASSWORD}
```

### 금지 사항
- 호스트/포트 하드코딩
- 토큰/시크릿 하드코딩
- 환경 의존적 경로

## 좋은 시나리오 예시

```gherkin
# language: ko
@e2e @payment
시나리오: 정상 결제 후 예약이 확정된다
  조건 Admin에 다음 상품이 등록되어 있다
    | name | price |
    | 텐트  | 50000 |
  만약 키오스크에서 결제를 생성한다
  그리고 키오스크에서 결제를 확정한다
  그러면 결제가 성공이어야 한다
  그리고 예약 상태는 "CONFIRMED"이어야 한다
```

**좋은 점:**
- 비즈니스 의도가 명확함
- When/Then이 기대 동작을 검증
- 환경 의존 없음
- 재실행 가능

## 나쁜 시나리오 예시

```gherkin
# language: ko
시나리오: 결제 테스트
  만약 "http://localhost:18081/api/payments"에 POST 요청을 보낸다
  그러면 상태 코드는 200이다
```

**문제점:**
- URL 하드코딩
- 비즈니스 의도 불명확
- 구현 세부사항 노출
- 단순 상태 코드만 검증

## 품질 기준 체크리스트

### 시나리오 제출 전 확인사항

- [ ] **의도-검증 일치**: When/Then이 비즈니스 기대를 검증하는가?
- [ ] **환경 의존 없음**: 호스트/포트/토큰 하드코딩 없는가?
- [ ] **데이터 격리**: 고유 네임스페이스 사용, 재실행 안전한가?
- [ ] **관찰 가능성**: 상태 변화를 검증할 수 있는가?
- [ ] **명확한 실패 메시지**: 실패 시 원인 파악이 쉬운가?

## 태그 규칙

### 테스트 유형 태그 (필수)

| 태그 | 용도 | 폴더 |
|------|------|------|
| `@smoke` | 서비스 헬스체크 (빠른 검증) | `features/smoke/` |
| `@e2e` | 전체 흐름 E2E 테스트 | `features/e2e/` |
| `@acceptance` | 비즈니스 인수 테스트 | `features/acceptance/` |

### 상태 태그

| 태그 | 용도 |
|------|------|
| `@ai-candidate` | AI 생성 후보 시나리오 (검토 대기) |
| `@wip` | 작업 중 (실행 제외) |

### 도메인 태그 (선택)

| 태그 | 용도 |
|------|------|
| `@payment` | 결제 관련 시나리오 |
| `@reservation` | 예약 관련 시나리오 |
| `@happy-path` | 정상 경로 |
| `@sad-path` | 실패 경로 |

### 태그 전환 프로세스

```
@acceptance @ai-candidate (AI 생성)
    ↓ 체크리스트 통과
@acceptance (검토 완료, @ai-candidate 제거)
```

## 폴더 구조

```
features/
├── smoke/           # @smoke - 서비스 헬스체크
│   └── health-check.feature
├── e2e/             # @e2e - 전체 흐름 테스트
│   ├── kiosk.feature
│   └── kiosk-payment.feature
└── acceptance/      # @acceptance - 비즈니스 인수 테스트
    ├── payment.feature
    └── reservation.feature
```

## 실행 명령어

```bash
# 스모크 테스트 (빠른 검증)
./gradlew test -Dcucumber.filter.tags="@smoke"

# E2E 테스트
./gradlew test -Dcucumber.filter.tags="@e2e"

# 인수 테스트
./gradlew test -Dcucumber.filter.tags="@acceptance"

# AI 후보 시나리오만 실행
./gradlew test -Dcucumber.filter.tags="@ai-candidate"

# 결제 시나리오만 실행
./gradlew test -Dcucumber.filter.tags="@payment"

# 특정 태그 제외
./gradlew test -Dcucumber.filter.tags="not @wip"

# 복합 조건
./gradlew test -Dcucumber.filter.tags="@acceptance and not @ai-candidate"
```

## WireMock 설정

### 구성 개요

외부 결제 서비스(PG사)를 WireMock으로 대체하여, 실제 결제 없이 E2E 테스트를 수행합니다.

```
[테스트 코드]                          [Docker Network: atdd-net]
     │                            ┌────────────┐     ┌──────────────┐
     │  http://localhost:18081    │            │     │   payments   │
     └──────────────────────────► │   kiosk    │────►│  (WireMock)  │
                                  │            │     │              │
                                  └────────────┘     └──────────────┘
                                   내부 호출:          컨테이너 포트: 8080
                                   http://payments:8080  호스트 포트: 19090
```

### Docker Compose 서비스 정의

```yaml
# infra/docker-compose.yml
payments:
  image: wiremock/wiremock:3.3.1
  ports:
    - "19090:8080"                                  # 호스트:컨테이너
  volumes:
    - ./wiremock/mappings:/home/wiremock/mappings    # 스텁 매핑 파일
    - ./wiremock/__files:/home/wiremock/__files      # 정적 응답 파일
  command: --global-response-templating             # 응답 템플릿 활성화
  networks:
    - atdd-net
```

Kiosk 서비스가 WireMock을 실제 결제 서비스로 인식하도록 환경변수를 설정합니다:

```yaml
# infra/docker-compose.yml - kiosk 서비스
environment:
  - KIOSK_PAYMENT_BASE_URL=http://payments:8080     # Docker 내부 URL
  - KIOSK_PAYMENT_SECRET_KEY=${PAYMENT_SECRET_KEY}
```

### URL 사용 규칙

| 사용 주체 | URL | 용도 |
|----------|-----|------|
| 테스트 코드 (호스트) | `http://localhost:19090` | WireMock readiness check, Admin API 조회 |
| Kiosk 컨테이너 (Docker 내부) | `http://payments:8080` | 결제 API 호출 (`/v1/payments`, `/v1/payments/confirm`) |

테스트 코드는 Kiosk API(`localhost:18081`)만 호출하면 되고, Kiosk가 내부적으로 WireMock과 통신합니다.
`localhost:19090`은 서비스 준비 상태 확인(`/__admin/mappings`)에만 사용됩니다.

### 스텁 매핑 파일

```
infra/wiremock/
├── mappings/
│   ├── payment-create.json           # POST /v1/payments → 200
│   ├── payment-confirm-success.json  # POST /v1/payments/confirm → 200 (priority: 10)
│   ├── payment-confirm-failure.json  # POST /v1/payments/confirm → 400 (priority: 1)
│   └── payment-approve.json          # POST /v1/payments → 200
└── __files/                          # 정적 응답 파일 (현재 미사용)
```

WireMock 기동 시 `mappings/` 폴더의 JSON 파일이 자동 로드됩니다.

### 성공/실패 분기 — Priority 기반

WireMock은 priority 숫자가 **낮을수록 우선** 매칭합니다:

| Priority | 조건 | 응답 | 파일 |
|----------|------|------|------|
| 1 (높음) | `amount == 12345` | 400 `INVALID_AMOUNT` | `payment-confirm-failure.json` |
| 10 (낮음) | 그 외 전부 | 200 `APPROVED` | `payment-confirm-success.json` |

실패 트리거: 결제 확정 시 `amount`를 `12345`로 보내면 실패 응답을 반환합니다.
추가 실패 케이스는 스텁 파일을 추가하여 구현합니다.

### 응답 템플릿

`--global-response-templating` 옵션으로 요청 데이터를 응답에 동적 반영합니다:

```json
{
  "paymentKey": "{{jsonPath request.body '$.paymentKey'}}",
  "orderId": "{{jsonPath request.body '$.orderId'}}",
  "totalAmount": "{{jsonPath request.body '$.amount'}}",
  "approvedAt": "{{now}}"
}
```

| 템플릿 함수 | 설명 |
|-------------|------|
| `{{jsonPath request.body '$.field'}}` | 요청 body에서 값 추출 |
| `{{now}}` | 현재 시간 |
| `{{randomValue type='UUID'}}` | 랜덤 UUID 생성 |

### WireMock Admin API

```bash
# 등록된 스텁 매핑 확인
curl http://localhost:19090/__admin/mappings

# 호출 기록 조회
curl http://localhost:19090/__admin/requests

# 매핑 초기화
curl -X POST http://localhost:19090/__admin/reset
```