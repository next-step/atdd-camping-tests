# ATDD Camping - AI 생성 인수 테스트 작성 가이드

## 목차
1. [시스템 개요](#시스템-개요)
2. [엔드포인트 요약](#엔드포인트-요약)
3. [사용자 여정](#사용자-여정)
4. [인증 및 시드 규칙](#인증-및-시드-규칙)
5. [Gherkin 작성 가이드](#gherkin-작성-가이드)
6. [품질 기준](#품질-기준)
7. [검토 게이트 체크리스트](#검토-게이트-체크리스트)

---

## 시스템 개요

### 아키텍처
ATDD Camping은 마이크로서비스 기반의 캠핑장 예약 시스템입니다.

```
┌─────────┐     ┌──────────┐     ┌──────────────┐
│ Kiosk   │────→│  Admin   │     │ Reservation  │
│ :18081  │     │  :18082  │     │   :18083     │
└────┬────┘     └──────────┘     └──────────────┘
     │
     ↓
┌─────────────┐
│  Payments   │
│  (WireMock) │
│   :18084    │
└─────────────┘
```

### 서비스 역할

#### 1. Kiosk (포트: 18081)
- **역할**: 키오스크 인터페이스, 고객 대면 서비스
- **책임**: 상품 조회, 결제 처리, 예약 요청
- **의존성**: Admin (인증, 상품 목록), Payments (결제 처리)

#### 2. Admin (포트: 18082)
- **역할**: 관리자 시스템
- **책임**: 상품 관리, 예약 조회/관리, 매출 통계
- **인증**: JWT 기반 (username: admin, password: admin123)

#### 3. Reservation (포트: 18083)
- **역할**: 예약 관리 시스템
- **책임**: 캠핑 사이트 예약 생성/조회/취소, 가용성 확인
- **비즈니스 규칙**:
  - 예약은 30일 이내만 가능
  - 당일 취소 시 환불 불가
  - 동시 예약 요청 시 단 하나만 성공 (동시성 제어)

#### 4. Payments (포트: 18084)
- **역할**: 외부 결제 시스템 (WireMock)
- **책임**: 결제 승인/실패 시뮬레이션
- **동작**:
  - 금액 < 100,000원: 성공
  - 금액 >= 100,000원: 실패

---

## 엔드포인트 요약

### Kiosk Service

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/products` | 상품 목록 조회 | Bearer Token |
| POST | `/api/payments` | 결제 생성 | No |
| POST | `/api/payments/confirm` | 결제 확정 | No |

### Admin Service

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/auth/login` | 관리자 로그인 | No |
| GET | `/admin/reservations` | 전체 예약 조회 | Bearer Token |
| PATCH | `/admin/reservations/{id}/status` | 예약 상태 변경 | Bearer Token |
| GET | `/admin/products` | 상품 목록 조회 | Bearer Token |

### Reservation Service

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/reservations` | 예약 생성 | No |
| GET | `/api/reservations/{id}` | 예약 조회 | No |
| GET | `/api/reservations` | 예약 목록 조회 | No |
| DELETE | `/api/reservations/{id}?confirmationCode={code}` | 예약 취소 | Confirmation Code |
| GET | `/api/reservations/my?name={name}&phone={phone}` | 내 예약 조회 | No |

### Payments Service (WireMock)

| Method | Endpoint | 설명 | 동작 |
|--------|----------|------|------|
| POST | `/v1/payments` | 결제 생성 | 항상 성공 |
| POST | `/v1/payments/confirm` | 결제 확정 | amount < 100k: 성공, >= 100k: 실패 |

---

## 사용자 여정

### 여정 1: 정상 예약 성공
```
1. 키오스크에서 예약 가능한 사이트 조회
2. 예약 정보 입력 (이름, 전화번호, 날짜, 사이트)
3. 예약 생성 요청 → Reservation 서비스
4. 결제 생성 → Payments 서비스
5. 결제 확정 → Payments 서비스 (성공)
6. 예약 상태 확인 → CONFIRMED
7. Admin에서 예약 조회 → 상태 확인
```

### 여정 2: 결제 실패 보상
```
1. 예약 생성 요청 → Reservation 서비스
2. 큰 금액으로 결제 생성
3. 결제 확정 → Payments 서비스 (실패)
4. 예약 상태 확인 → FAILED 또는 미확정
5. Admin에서 예약 조회 → 실패 상태 확인
```

### 여정 3: 중복 예약 방지 (Idempotency)
```
1. 동일한 예약 정보로 첫 번째 요청
2. 동일한 예약 정보로 두 번째 요청 (빠르게)
3. 하나의 예약만 생성됨을 확인
4. 두 번째 요청은 실패 또는 첫 번째 예약 반환
```

### 여정 4: 취소/환불
```
1. 예약 생성 및 결제 완료
2. 예약 취소 요청 (confirmationCode 필요)
3. 환불 처리 확인
4. Admin에서 취소 상태 확인
5. 사이트 가용성 복원 확인
```

### 여정 5: 관리자 개입
```
1. Admin 로그인
2. 특정 예약 조회
3. Admin에서 예약 상태 수동 변경 (CONFIRMED → CANCELLED)
4. 변경 사항 반영 확인
```

---

## 인증 및 시드 규칙

### 인증

#### Admin 서비스 인증
```gherkin
만약 관리자로 로그인한다
```

**요청 예시:**
```json
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

**응답:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Reservation 서비스
- 대부분의 API는 인증 불필요
- 예약 취소 시 `confirmationCode` 필요 (예약 생성 시 자동 생성된 6자리 코드)

### 시드 데이터

#### 사전 설정된 상품 (Admin)
- 랜턴: 30,000원
- 텐트: 150,000원
- 침낭: 50,000원

#### 사전 설정된 캠핑 사이트 (Reservation)
- A01, A02, A03 (대형)
- B01, B02, B03 (소형)

### 데이터 격리 규칙

각 시나리오는 독립적으로 실행 가능해야 합니다:
1. **고유 네임스페이스 사용**: 예약자 이름에 timestamp 또는 UUID 추가
   ```
   예: "홍길동-ai-test-1234567890"
   ```

2. **테스트 데이터 정리**: `@After` 훅에서 생성된 데이터 정리 (선택사항)

3. **환경 변수 외부화**: 호스트/포트 하드코딩 금지
   ```java
   String baseUrl = System.getenv("KIOSK_BASE_URL");
   if (baseUrl == null) baseUrl = "http://localhost:18081";
   ```

---

## Gherkin 작성 가이드

### 기본 원칙

1. **비즈니스 언어 사용**: 기술 용어보다 도메인 언어 사용
2. **의도 중심 작성**: "무엇"을 테스트하는지 명확히
3. **검증 명확화**: Given-When-Then 패턴 준수

### 좋은 예시

#### ✅ 좋은 시나리오 1: 비즈니스 의도 명확
```gherkin
@ai-candidate
시나리오: 정상 예약 후 결제 성공 시 예약이 확정된다
  배경
    만약 예약 가능한 캠핑 사이트가 있다

  만약 고객이 "A01" 사이트를 "2025-01-15"부터 "2025-01-17"까지 예약한다
  그리고 고객이 예약에 대해 결제를 완료한다
  그러면 예약 상태가 "CONFIRMED"이다
  그리고 관리자 시스템에서 해당 예약을 조회할 수 있다
  그리고 관리자 시스템에서 예약 상태가 "PAID"이다
```

#### ✅ 좋은 시나리오 2: 보상 트랜잭션
```gherkin
@ai-candidate
시나리오: 결제 실패 시 예약이 확정되지 않는다
  만약 고객이 "A01" 사이트를 예약한다
  그리고 고객이 예약에 대해 큰 금액으로 결제를 시도한다
  그러면 결제가 실패한다
  그리고 예약 상태가 "PENDING" 또는 "FAILED"이다
  그리고 사이트 "A01"의 가용성이 복원된다
```

#### ✅ 좋은 시나리오 3: 멱등성
```gherkin
@ai-candidate
시나리오: 동일한 예약 요청을 중복 전송해도 단 하나의 예약만 생성된다
  만약 고객이 "A01" 사이트에 대해 동일한 예약 요청을 2번 전송한다
  그러면 단 하나의 예약만 생성된다
  그리고 두 번째 요청은 실패하거나 첫 번째 예약을 반환한다
```

### 나쁜 예시

#### ❌ 나쁜 시나리오 1: 기술적 세부사항 노출
```gherkin
시나리오: POST /api/reservations 호출 시 201 응답
  만약 "http://localhost:18083/api/reservations"에 POST 요청을 보낸다
  그리고 요청 바디에 JSON을 포함한다
  그러면 응답 코드가 201이다
  그리고 응답 헤더에 "Location"이 있다
```
**문제점**: HTTP 프로토콜 세부사항에 집중, 비즈니스 의도 불명확

#### ❌ 나쁜 시나리오 2: Given-When-Then 혼재
```gherkin
시나리오: 예약 생성
  만약 사이트를 예약한다
  그리고 상태를 확인한다
  그러면 예약이 생성된다
  그리고 결제를 한다
```
**문제점**: 단계 순서가 불명확, 검증이 중간에 있음

#### ❌ 나쁜 시나리오 3: 환경 의존적
```gherkin
시나리오: 로컬 환경에서 예약 생성
  만약 "localhost:18083"에 접속한다
  그리고 DB에 직접 INSERT한다
  그러면 테이블에 레코드가 있다
```
**문제점**: 환경 하드코딩, DB 직접 조작

---

## 품질 기준

### 1. 의도-검증 일치
- **When** 절: 사용자 행동/시스템 이벤트
- **Then** 절: 비즈니스 기대치 검증

### 2. 환경 독립성
- 호스트/포트 하드코딩 금지
- 환경 변수 또는 설정 파일 사용
- 예: `KIOSK_BASE_URL`, `ADMIN_BASE_URL`, `RESERVATION_BASE_URL`

### 3. 데이터 격리
- 각 시나리오는 독립 실행 가능
- 고유 식별자 사용 (timestamp, UUID)
- 재실행 안전성 보장

### 4. 관찰 가능성
- **Reservation 상태 확인**: 예약 생성 후 상태 검증
- **Admin 조회**: 관리자 시스템에서 데이터 일관성 확인
- **WireMock 호출 기록**: 외부 시스템 통신 검증

### 5. 커버리지
- **정상 시나리오**: Happy path
- **경계 시나리오**: Edge cases (날짜 제약, 재고 부족)
- **예외 시나리오**: 실패 케이스 및 보상 트랜잭션

---

## 검토 게이트 체크리스트

AI 생성 시나리오는 `@ai-candidate` 태그로 제출됩니다. 다음 체크리스트를 통과하면 `@ai-candidate` 태그를 제거하고 `@e2e` 태그로 승격합니다.

### ✅ 체크리스트

#### 1. 의도-검증 일치
- [ ] When 절이 명확한 사용자 행동을 나타내는가?
- [ ] Then 절이 비즈니스 기대치를 검증하는가?
- [ ] 기술적 세부사항(HTTP 코드, JSON 필드)보다 비즈니스 개념을 우선하는가?

#### 2. 환경 의존 없음
- [ ] 호스트/포트가 하드코딩되지 않았는가?
- [ ] 환경 변수로 BASE_URL을 외부화했는가?
- [ ] 토큰/시크릿이 코드에 노출되지 않았는가?

#### 3. 데이터 격리
- [ ] 고유 네임스페이스를 사용하는가? (예: `홍길동-ai-1234567890`)
- [ ] 재실행 시 이전 실행의 데이터와 충돌하지 않는가?
- [ ] 병렬 실행이 가능한가?

#### 4. 관찰 가능성
- [ ] Reservation 서비스의 상태를 검증하는가?
- [ ] Admin 시스템에서 데이터 일관성을 확인하는가?
- [ ] 필요 시 WireMock 호출 기록을 검증하는가?

#### 5. 명확성과 가독성
- [ ] 시나리오 제목이 테스트 의도를 명확히 전달하는가?
- [ ] Given-When-Then 구조가 올바른가?
- [ ] 한국어 표현이 자연스럽고 이해하기 쉬운가?

#### 6. 완전성
- [ ] 모든 전제 조건(Given)이 명시되었는가?
- [ ] 실행(When)이 하나의 명확한 행동인가?
- [ ] 검증(Then)이 충분한가?

### 승격 프로세스

1. **제출**: AI가 시나리오를 생성하고 `@ai-candidate` 태그 부여
2. **검토**: 위 체크리스트로 품질 검증
3. **수정**: 문제가 있으면 시나리오 수정
4. **승격**: 체크리스트 통과 시 `@ai-candidate` 제거, `@e2e` 추가
5. **실행**: `./gradlew test -Dcucumber.filter.tags="@e2e"` 실행

---

## 추가 태그

프로젝트에서 사용 가능한 태그:

- `@ai-candidate`: AI가 생성한 테스트 후보
- `@e2e`: 검증 완료된 E2E 테스트
- `@smoke`: 빠른 헬스 체크 테스트
- `@wip`: Work In Progress (개발 중)
- `@skip`: 일시적으로 비활성화된 테스트

### 태그 조합 예시

```gherkin
@ai-candidate @reservation @happy-path
시나리오: 정상 예약 성공

@ai-candidate @payment @failure @compensation
시나리오: 결제 실패 보상

@ai-candidate @concurrency @idempotency
시나리오: 중복 예약 방지
```

---

## AI 프롬프트 예시

AI 도구를 활용하여 시나리오를 생성할 때 다음과 같은 프롬프트를 사용하세요:

```
당신은 ATDD Camping 시스템의 인수 테스트를 작성하는 QA 엔지니어입니다.

시스템: 캠핑장 예약 시스템 (Kiosk, Admin, Reservation, Payments 서비스)
테스트 프레임워크: Cucumber (Gherkin)
언어: 한국어

다음 시나리오를 Gherkin으로 작성해주세요:
1. 정상 예약 성공 (결제 포함)
2. 결제 실패 시 보상 트랜잭션
3. 동시 예약 요청 시 멱등성 보장

요구사항:
- @ai-candidate 태그 사용
- 비즈니스 언어로 작성 (기술 세부사항 최소화)
- Given-When-Then 패턴 준수
- 환경 변수로 BASE_URL 외부화
- 고유 식별자 사용으로 데이터 격리

참고: acceptance-test-guide.md의 품질 기준을 준수해주세요.
```

---

## 참고 자료

- [Cucumber Best Practices](https://cucumber.io/docs/gherkin/reference/)
- [Given-When-Then 패턴](https://martinfowler.com/bliki/GivenWhenThen.html)
- 프로젝트 README: `/README.md`
- 인증 설정 가이드: `/docs/auth-setup.md`
