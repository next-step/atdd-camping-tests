# WireMock

## WireMock이란?

HTTP 기반 API를 모킹(Mocking)하는 도구. 외부 서비스에 의존하지 않고 테스트할 수 있게 해준다.

```
┌─────────┐      ┌──────────┐      ┌─────────────┐
│  Kiosk  │ ───► │ WireMock │ ───► │ (실제 결제) │
│ Service │      │ (Mock)   │      │   서비스    │
└─────────┘      └──────────┘      └─────────────┘
                      │
               테스트 시에는
               여기서 응답 반환
```

## 왜 사용하는가?

| 문제 | WireMock 해결책 |
|------|-----------------|
| 외부 API 불안정 | 항상 동일한 응답 반환 |
| 테스트 비용 (실제 결제) | 무료로 무한 호출 |
| 에러 케이스 재현 어려움 | 원하는 에러 응답 설정 |
| 네트워크 지연 | 로컬에서 빠르게 응답 |

## 적용 방식

### 1. 실행 방식 선택

| 방식 | 장점 | 단점 |
|------|------|------|
| Docker Compose 서비스 ✓ | 인프라와 함께 관리, CI 친화적 | 컨테이너 오버헤드 |
| 로컬 단독 실행 | 빠른 시작 | 수동 관리 필요 |
| 테스트 코드 내장 | 테스트와 밀접 | 설정 복잡 |

선택: Docker Compose - 다른 서비스들과 동일한 네트워크에서 관리

### 2. 스텁 관리 방식

| 방식 | 장점 | 단점 |
|------|------|------|
| 정적 매핑 파일 ✓ | 버전 관리, 재사용 | 동적 변경 어려움 |
| Admin API 런타임 등록 | 동적 설정 | 테스트마다 설정 필요 |

선택: 정적 매핑 - `mappings/*.json` 파일로 기동 시 자동 로드

## 스텁 매핑 작성법

### 기본 구조

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/v1/payments"
  },
  "response": {
    "status": 200,
    "headers": { "Content-Type": "application/json" },
    "jsonBody": { "status": "APPROVED" }
  }
}
```

### 요청 매칭

```json
// URL 매칭
"urlPath": "/v1/payments"           // 정확히 일치
"urlPathPattern": "/v1/payments/.*" // 정규식

// Body 매칭
"bodyPatterns": [
  { "matchesJsonPath": "$[?(@.amount == 12345)]" }
]
```

### 우선순위 (Priority)

```json
// priority 값이 낮을수록 먼저 매칭
{ "priority": 1, ... }  // 먼저 검사 (특수 케이스)
{ "priority": 10, ... } // 나중에 검사 (기본 케이스)
```

예시:
- Priority 1: `amount == 12345` → 실패 응답
- Priority 10: 그 외 모든 요청 → 성공 응답

### Response Templating

```json
{
  "response": {
    "jsonBody": {
      "paymentKey": "{{jsonPath request.body '$.paymentKey'}}",
      "approvedAt": "{{now}}"
    },
    "transformers": ["response-template"]
  }
}
```

- `{{jsonPath request.body '$.field'}}` - 요청 body에서 값 추출
- `{{now}}` - 현재 시간
- `{{randomValue type='UUID'}}` - 랜덤 UUID

## 프로젝트 적용

### 디렉토리 구조

```
infra/wiremock/
├── mappings/
│   ├── payment-create.json          # POST /v1/payments
│   ├── payment-confirm-success.json # 성공 케이스 (priority: 10)
│   └── payment-confirm-failure.json # 실패 케이스 (priority: 1)
└── __files/                         # 정적 응답 파일 (필요시)
```

### Docker Compose 설정

```yaml
payments:
  image: wiremock/wiremock:3.3.1
  ports:
    - "19090:8080"
  volumes:
    - ./wiremock/mappings:/home/wiremock/mappings
    - ./wiremock/__files:/home/wiremock/__files
  command: --global-response-templating
  networks:
    - atdd-net
```

### Kiosk 서비스 연결

```yaml
# Kiosk가 WireMock을 실제 결제 서비스로 인식
environment:
  - KIOSK_PAYMENT_BASE_URL=http://payments:8080
```

## 테스트 시나리오

### Happy Path (성공)

```
Kiosk → POST /api/payments → WireMock → 200 OK
Kiosk → POST /api/payments/confirm → WireMock → 200 APPROVED
```

### Sad Path (실패)

```
Kiosk → POST /api/payments/confirm (amount=12345)
     → WireMock → 400 INVALID_AMOUNT
```

## Admin API 활용

WireMock은 `/__admin` 엔드포인트로 런타임 관리 가능:

```bash
# 등록된 매핑 확인
curl http://localhost:19090/__admin/mappings

# 호출 기록 확인
curl http://localhost:19090/__admin/requests

# 매핑 초기화
curl -X POST http://localhost:19090/__admin/reset
```

## 배운 점

1. 외부 의존성 격리: 결제 서비스 없이도 E2E 테스트 가능
2. 에러 시나리오 테스트: 실제로 발생시키기 어려운 에러 케이스 쉽게 테스트
3. Priority 기반 매칭: 특수 케이스와 기본 케이스를 우선순위로 구분
4. Response Templating: 요청 데이터를 응답에 동적으로 반영
5. Docker 통합: 다른 서비스들과 동일한 네트워크에서 일관되게 관리