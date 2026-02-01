# 캠핑 키오스크 시스템 분석

## 프로젝트 개요

캠핑 용품점 키오스크 시스템 - Spring Boot 기반 REST API 애플리케이션으로, 외부 Admin Server와 Payment Server와 통신하여 상품 조회 및 결제를 처리합니다.

---

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Thymeleaf (템플릿 엔진)
- Spring Retry (재시도 로직)
- WireMock (HTTP Mock 테스트)
- Cucumber (ATDD/BDD 테스트)


---

## 주요 설정값
## 프로젝트 구성

| 서비스 | 프로젝트 | 내부 포트 | DB | 의존 서비스 |
|:---|:---|:---:|:---:|:---|
| Admin | `atdd-camping-admin` | 8080 | H2 | - |
| Reservation | `atdd-camping-reservation` | 8080 | H2 | - |
| Kiosk | `atdd-camping-kiosk` | 8080 | - | Admin |

```yaml
server:
  port: 8080
---

kiosk:
  admin:
    base-url: http://localhost:8080
    auth:
      username: admin
      password: admin123
      login-path: /auth/login
      cookie-name: AUTH_TOKEN
  payment:
    base-url: http://localhost:9090
    secret-key: ${PAYMENTS_SECRET_KEY:test_sk_dummy}
  discount:
    percent: 10
## Docker Compose 포트 매핑

# RestClient 설정
connect-timeout: 2초
read-timeout: 3초
```
| 서비스 | 호스트 포트 | 접속 URL |
|:---|:---:|:---|
| admin | 18080 | http://localhost:18080 |
| kiosk | 18081 | http://localhost:18081 |
| reservation | 18082 | http://localhost:18082 |

실행: `cd infra && docker-compose up --build`

---

## API 엔드포인트 목록

| HTTP | 엔드포인트 | 기능 | 요청 | 응답 |
|:---:|:---|:---|:---|:---|
| GET | `/health` | 헬스체크 | - | `"OK"` |
| GET | `/` | 메인 페이지 (상품 목록 표시) | - | HTML |
| GET | `/api/products` | 상품 목록 조회 | - | `List<Product>` |
| POST | `/api/payments` | 결제 생성 (트랜잭션 초기화) | `{items, paymentMethod}` | `{paymentKey, orderId, amount}` |
| POST | `/api/payments/confirm` | 결제 승인 확정 | `{paymentKey, orderId, items}` | `{transactionId, paidAmount}` |

---

## 외부 시스템 연동 API

| 대상 서버 | 메서드 | 엔드포인트 | 기능 |
|:---|:---:|:---|:---|
| Admin Server | POST | `/auth/login` | 인증 토큰 획득 |
| Admin Server | GET | `/admin/products` | 상품 목록 조회 |
| Admin Server | POST | `/api/sales` | 판매 확정 |
| Payment Server | POST | `/v1/payments` | 결제 생성 |
| Payment Server | POST | `/v1/payments/confirm` | 결제 승인 |
| Payment Server | POST | `/v1/payments/{key}/cancel` | 환불 처리 |

---

## 리스크 메트릭 기반 우선순위 분석

| 우선순위 | 기능 | 리스크 요소 | 복잡도 | 영향도 | 권장 테스트 |
|:---:|:---|:---|:---:|:---:|:---|
| 1 | 결제 승인 (`/api/payments/confirm`) | 금전 거래, 외부 시스템 2개 연동, 재시도 로직 | 높음 | 매우 높음 | E2E, 실패 시나리오, 재시도 검증 |
| 2 | 결제 생성 (`/api/payments`) | 금전 거래 시작점, Payment Server 의존 | 중간 | 높음 | 통합 테스트, 타임아웃 검증 |
| 3 | Admin 인증 (토큰/쿠키) | 보안, 캐시 무효화 로직 | 중간 | 높음 | 인증 실패/만료 시나리오 |
| 4 | 상품 조회 (`/api/products`) | Admin Server 의존, 인증 필요 | 낮음 | 중간 | API 장애 시 동작 검증 |
| 5 | 장바구니 기능 (프론트엔드) | 클라이언트 상태 관리 | 낮음 | 중간 | UI 테스트, 수량 계산 |
| 6 | 헬스체크 (`/health`) | 없음 | 낮음 | 낮음 | 단순 응답 확인 |

---

## 핵심 리스크 상세

### 1. 결제 승인 프로세스 (최고 리스크)

```
[결제 승인 요청] → [Payment Server 승인] → [Admin Server 판매 확정]
                                              ↳ 실패 시 3회 재시도 (200ms 간격)
```

- 리스크: Payment 승인 성공 후 Admin 판매 확정 실패 시 데이터 불일치
- 권장 테스트:
  - Admin Server 장애 시 재시도 동작
  - 부분 실패 시 롤백/보상 트랜잭션

### 2. 인증 캐싱 메커니즘

- 리스크: 토큰 만료 후 캐시 클리어 타이밍 이슈
- 권장 테스트: 인증 만료 → 재인증 → API 재호출 흐름

### 3. 타임아웃 설정

- Connect: 2초, Read: 3초
- 리스크: 외부 서버 지연 시 사용자 경험 저하
- 권장 테스트: 타임아웃 발생 시 에러 핸들링

---

## 테스트 우선순위 매트릭스

| 기능 | 비즈니스 영향 | 기술 복잡도 | 외부 의존성 | 종합 점수 |
|:---|:---:|:---:|:---:|:---:|
| 결제 승인 | 10 | 8 | 9 | 27 |
| 결제 생성 | 9 | 6 | 7 | 22 |
| Admin 인증 | 7 | 7 | 6 | 20 |
| 상품 조회 | 6 | 4 | 5 | 15 |
| 장바구니 | 5 | 3 | 0 | 8 |
| 헬스체크 | 2 | 1 | 0 | 3 |

---

## 아키텍처 구성도

```
┌─────────────────────────────────────────────────────────────────┐
│                        Kiosk Application                        │
│                      (Spring Boot :8080)                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌──────────────────┐  ┌───────────────────┐  │
│  │ HomeController│  │ ProductController │  │ PaymentController │  │
│  │  GET /       │  │ GET /api/products│  │ POST /api/payments│  │
│  │  GET /health │  │                  │  │ POST /confirm     │  │
│  └─────────────┘  └────────┬─────────┘  └─────────┬─────────┘  │
│                            │                       │            │
│                    ┌───────▼───────────────────────▼───────┐    │
│                    │           Service Layer               │    │
│                    │  ┌─────────────┐  ┌───────────────┐   │    │
│                    │  │AdminService │  │PaymentService │   │    │
│                    │  └──────┬──────┘  └───────┬───────┘   │    │
│                    └─────────┼─────────────────┼───────────┘    │
│                              │                 │                │
│                    ┌─────────▼─────────────────▼───────────┐    │
│                    │         External Clients              │    │
│                    │  ┌────────────┐  ┌──────────────┐     │    │
│                    │  │AdminClient │  │PaymentClient │     │    │
│                    │  │AuthClient  │  │              │     │    │
│                    │  └─────┬──────┘  └──────┬───────┘     │    │
│                    └────────┼────────────────┼─────────────┘    │
└─────────────────────────────┼────────────────┼──────────────────┘
                              │                │
              ┌───────────────▼──┐      ┌──────▼───────────┐
              │   Admin Server   │      │  Payment Server  │
              │   (External)     │      │   (External)     │
              │                  │      │                  │
              │ - /auth/login    │      │ - /v1/payments   │
              │ - /admin/products│      │ - /v1/confirm    │
              │ - /api/sales     │      │ - /v1/cancel     │
              └──────────────────┘      └──────────────────┘
```

