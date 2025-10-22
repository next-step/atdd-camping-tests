# ATDD Camping - 인증 및 실행 가이드

## 목차
1. [환경 설정](#환경-설정)
2. [인증 방법](#인증-방법)
3. [테스트 실행](#테스트-실행)
4. [디버깅](#디버깅)
5. [문제 해결](#문제-해결)

---

## 환경 설정

### 필수 요구사항
- Java 17 이상
- Gradle 7.x 이상
- Docker & Docker Compose
- MySQL 8.0 (Docker로 자동 설치)

### 환경 변수

테스트 실행 시 다음 환경 변수를 설정할 수 있습니다:

```bash
# 서비스 Base URL (선택사항, 기본값 있음)
export KIOSK_BASE_URL=http://localhost:18081
export ADMIN_BASE_URL=http://localhost:18082
export RESERVATION_BASE_URL=http://localhost:18083
export PAYMENTS_BASE_URL=http://localhost:18084

# CI/CD 환경에서는 도메인 사용 가능
export KIOSK_BASE_URL=https://kiosk.atdd-camping.com
export ADMIN_BASE_URL=https://admin.atdd-camping.com
export RESERVATION_BASE_URL=https://reservation.atdd-camping.com
```

### 기본값
환경 변수가 설정되지 않은 경우 다음 기본값이 사용됩니다:
- Kiosk: `http://localhost:18081`
- Admin: `http://localhost:18082`
- Reservation: `http://localhost:18083`
- Payments: `http://localhost:18084`

---

## 인증 방법

### 1. Admin 서비스 인증

Admin 서비스는 JWT 기반 인증을 사용합니다.

#### 로그인 요청
```http
POST {ADMIN_BASE_URL}/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 응답
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 인증 토큰 사용
```http
GET {ADMIN_BASE_URL}/admin/reservations
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Reservation 서비스 인증

대부분의 Reservation API는 인증이 필요하지 않습니다. 단, 예약 취소 시 **Confirmation Code**가 필요합니다.

#### Confirmation Code
- 예약 생성 시 자동 생성되는 6자리 영숫자 코드
- 예약 취소/수정 시 본인 확인 용도

**예약 생성 응답:**
```json
{
  "id": 1,
  "customerName": "홍길동",
  "phone": "010-1234-5678",
  "confirmationCode": "ABC123",
  "status": "PENDING"
}
```

**예약 취소 요청:**
```http
DELETE {RESERVATION_BASE_URL}/api/reservations/1?confirmationCode=ABC123
```

### 3. Kiosk 및 Payments 서비스

인증이 필요하지 않습니다.

---

## 테스트 실행

### 1. 서비스 기동

#### 전체 서비스 기동 (권장)
```bash
# 모든 서비스 Clone → Build → 기동
./gradlew kioskUp

# 특정 브랜치로 기동
./gradlew kioskUp -Pbranch=develop
```

#### 서비스 상태 확인
```bash
./gradlew kioskStatus

# 또는 Docker 명령어
docker ps | grep -E 'kiosk|admin|reservation|payments-mock'
```

#### 개별 서비스 확인
```bash
# Kiosk 헬스 체크
curl http://localhost:18081/

# Admin 헬스 체크
curl http://localhost:18082/admin

# Reservation 헬스 체크
curl http://localhost:18083/

# Payments (WireMock) 헬스 체크
curl http://localhost:18084/__admin/health
```

### 2. 테스트 실행

#### 전체 테스트 실행
```bash
./gradlew test
```

#### 태그 기반 실행

##### Smoke Test (빠른 헬스 체크)
```bash
./gradlew testSmoke
```

##### AI 후보 테스트 실행
```bash
./gradlew test -Dcucumber.filter.tags="@ai-candidate"
```

##### E2E 테스트 실행
```bash
./gradlew test -Dcucumber.filter.tags="@e2e"
```

##### 특정 기능 테스트
```bash
# 예약 관련 테스트만 실행
./gradlew test -Dcucumber.filter.tags="@reservation"

# 결제 관련 테스트만 실행
./gradlew test -Dcucumber.filter.tags="@payment"

# 보상 트랜잭션 테스트만 실행
./gradlew test -Dcucumber.filter.tags="@compensation"
```

##### 태그 조합
```bash
# AI 후보 중 예약 관련 테스트
./gradlew test -Dcucumber.filter.tags="@ai-candidate and @reservation"

# E2E 테스트 중 보상 트랜잭션 제외
./gradlew test -Dcucumber.filter.tags="@e2e and not @compensation"
```

### 3. 병렬 실행

```bash
# Gradle 병렬 실행 (주의: 데이터 격리 필수)
./gradlew test --parallel --max-workers=4
```

### 4. 특정 Feature 파일 실행

```bash
# 특정 feature 파일만 실행
./gradlew test -Dcucumber.features=src/test/resources/features/ai-generated/reservation-success.feature
```

---

## 디버깅

### 1. 로그 확인

#### 서비스 로그
```bash
# Kiosk 로그
docker logs kiosk -f

# Admin 로그
docker logs admin -f

# Reservation 로그
docker logs reservation -f

# Payments (WireMock) 로그
docker logs payments-mock -f
```

#### 전체 로그
```bash
./gradlew kioskLogs
```

### 2. WireMock 검증

WireMock은 결제 서비스를 모의합니다. 호출 기록을 확인하려면:

```bash
# WireMock 관리 API
curl http://localhost:18084/__admin/requests

# 특정 매핑 확인
curl http://localhost:18084/__admin/mappings
```

#### WireMock 매핑 파일 위치
```
wiremock/
├── mappings/
│   ├── payment-confirm.json
│   ├── payment-success.json
│   ├── payment-declined.json
│   └── health-check.json
└── __files/
```

### 3. 데이터베이스 확인

#### MySQL 접속
```bash
docker exec -it atdd-db mysql -uroot -psecret atdd
```

#### 주요 테이블 조회
```sql
-- 예약 확인
SELECT * FROM reservations ORDER BY created_at DESC LIMIT 10;

-- 예약 상태별 집계
SELECT status, COUNT(*) FROM reservations GROUP BY status;

-- 최근 생성된 예약
SELECT id, customer_name, phone, confirmation_code, status, created_at
FROM reservations
WHERE customer_name LIKE '%ai-test%'
ORDER BY created_at DESC;
```

### 4. RestAssured 디버깅

테스트 코드에서 요청/응답을 상세히 출력하려면:

```java
RestAssured.given()
    .log().all()  // 요청 전체 로깅
    .contentType("application/json")
    .body(requestBody)
    .when()
    .post(url)
    .then()
    .log().all()  // 응답 전체 로깅
    .statusCode(200);
```

### 5. Cucumber 리포트

테스트 실행 후 리포트 확인:
```bash
open build/reports/tests/test/index.html
```

---

## 문제 해결

### 1. 서비스가 기동되지 않음

#### 증상
```
curl: (7) Failed to connect to localhost port 18081: Connection refused
```

#### 해결
```bash
# 서비스 상태 확인
docker ps -a | grep -E 'kiosk|admin|reservation'

# 로그 확인
docker logs kiosk

# 재기동
./gradlew kioskDown
./gradlew kioskUp
```

### 2. 인증 실패

#### 증상
```
401 Unauthorized: Invalid token
```

#### 해결
1. Admin 로그인이 선행되었는지 확인
2. 토큰이 만료되지 않았는지 확인 (기본 1시간)
3. Authorization 헤더 형식 확인: `Bearer {token}`

```gherkin
# 올바른 순서
만약 관리자로 로그인한다
그리고 "Admin" 서비스에 상품 목록 요청을 보낸다
```

### 3. 예약 취소 실패

#### 증상
```
400 Bad Request: Invalid confirmation code
```

#### 해결
1. Confirmation code가 올바른지 확인
2. 예약 ID와 confirmation code가 매칭되는지 확인
3. 예약이 이미 취소되지 않았는지 확인

```java
// 예약 생성 시 confirmation code 저장
String confirmationCode = response.jsonPath().getString("confirmationCode");
ContextHelper.set("confirmationCode", confirmationCode);

// 예약 취소 시 사용
String code = ContextHelper.get("confirmationCode", String.class);
```

### 4. 결제 실패

#### 증상
```
결제가 성공이어야 한다 - Expected success to be true, but got false
```

#### 해결
WireMock 설정 확인:
- 금액 < 100,000원: 성공
- 금액 >= 100,000원: 실패

```java
// 성공 케이스
String items = "[{\"productId\":1,\"productName\":\"랜턴\",\"unitPrice\":30000,\"quantity\":1}]";

// 실패 케이스 (의도적)
String items = "[{\"productId\":1,\"productName\":\"텐트\",\"unitPrice\":999999,\"quantity\":1}]";
```

### 5. 동시성 테스트 실패

#### 증상
```
Expected only 1 reservation, but got 2
```

#### 해결
1. Reservation 서비스의 동시성 제어 확인
2. 트랜잭션 격리 수준 확인
3. 테스트 타이밍 조정

```java
// 동시 요청 시뮬레이션
ExecutorService executor = Executors.newFixedThreadPool(2);
List<Future<Response>> futures = new ArrayList<>();

for (int i = 0; i < 2; i++) {
    futures.add(executor.submit(() -> {
        return RestAssured.given()
            .body(requestBody)
            .post(url);
    }));
}

// 결과 검증
long successCount = futures.stream()
    .map(f -> f.get().getStatusCode())
    .filter(code -> code == 201)
    .count();

assert successCount == 1 : "Expected 1 success, got " + successCount;
```

### 6. 테스트 데이터 중복

#### 증상
```
409 Conflict: Duplicate reservation for the same period
```

#### 해결
고유 식별자 사용:
```java
long timestamp = System.currentTimeMillis();
String uniqueName = "고객-ai-test-" + timestamp;
String uniquePhone = "010-" + (timestamp % 100000000);
```

### 7. 환경 변수가 적용되지 않음

#### 증상
테스트가 localhost 대신 다른 URL로 요청을 보내야 하는데 안 됨

#### 해결
```bash
# 환경 변수 설정 확인
echo $KIOSK_BASE_URL

# 환경 변수와 함께 테스트 실행
KIOSK_BASE_URL=http://staging.example.com:8080 ./gradlew test

# 또는 .env 파일 사용
export $(cat .env | xargs)
./gradlew test
```

### 8. Gradle 빌드 실패

#### 증상
```
Task :test FAILED
```

#### 해결
```bash
# 클린 빌드
./gradlew clean build

# 의존성 재다운로드
./gradlew build --refresh-dependencies

# 캐시 삭제
rm -rf ~/.gradle/caches
./gradlew build
```

---

## 성능 최적화

### 1. 테스트 실행 속도 개선

```bash
# 병렬 실행
./gradlew test --parallel --max-workers=4

# 증분 빌드
./gradlew test --build-cache
```

### 2. Docker 빌드 캐시 활용

```bash
# Docker BuildKit 활성화
export DOCKER_BUILDKIT=1
./gradlew kioskUp
```

### 3. 선택적 테스트 실행

```bash
# 빠른 smoke 테스트만
./gradlew testSmoke

# 특정 태그만 실행
./gradlew test -Dcucumber.filter.tags="@smoke or @fast"
```

---

## CI/CD 통합

### GitHub Actions 예시

```yaml
name: ATDD Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Docker
        uses: docker/setup-buildx-action@v2

      - name: Start services
        run: ./gradlew kioskUp

      - name: Run smoke tests
        run: ./gradlew testSmoke

      - name: Run E2E tests
        run: ./gradlew test -Dcucumber.filter.tags="@e2e"

      - name: Run AI candidate tests
        run: ./gradlew test -Dcucumber.filter.tags="@ai-candidate"
        continue-on-error: true

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: build/reports/tests/

      - name: Cleanup
        if: always()
        run: ./gradlew kioskDown
```

---

## 참고 자료

- [Cucumber 공식 문서](https://cucumber.io/docs/cucumber/)
- [RestAssured 사용 가이드](https://rest-assured.io/)
- [WireMock 문서](http://wiremock.org/docs/)
- [프로젝트 README](/README.md)
- [테스트 작성 가이드](/docs/acceptance-test-guide.md)
