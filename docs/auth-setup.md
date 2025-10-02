# 인증/실행 가이드

> AI(Claude)가 테스트 환경 설정 및 실행 흐름을 이해하기 위한 가이드
>
> **테스트 코드 작성은 `acceptance-test-guide.md` 참조**

## 실행 흐름

### 1. 초기 설정 (최초 1회)

```bash
# 서비스 저장소 클론 (kiosk, admin, reservation)
./gradlew SetupAllServices
```

**동작**:
- `repos/` 디렉터리 생성
- GitHub에서 3개 서비스 클론:
  - `repos/atdd-camping-kiosk`
  - `repos/atdd-camping-admin`
  - `repos/atdd-camping-reservation`
- `parkSeryu` 브랜치 체크아웃

### 2. 환경 시작

```bash
# 인프라 + 애플리케이션 서비스 시작
./gradlew ComposeAllUp
```

**시작 순서**:
1. **Infra** (`docker-compose-infra.yml`): MySQL 데이터베이스
2. **Was** (`docker-compose.yml`): admin → reservation → payments-mock → kiosk

**헬스체크**:
- DB: `mysqladmin ping` (최대 20회 재시도, 5초 간격)
- 서비스: `curl http://localhost:8080` (각각 최대 20회 재시도)
- WireMock: `curl http://localhost:8080/__admin/mappings`

**의존성**:
```
kiosk는 다음이 healthy 상태여야 시작:
- admin (service_healthy)
- payments-mock (service_healthy)
```

### 3. 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 태그 테스트 실행
./gradlew test -Dcucumber.filter.tags="@smoke"
```

### 4. 환경 종료

```bash
# 모든 서비스 중지 + 볼륨 삭제
./gradlew ComposeAllDown
```

**종료 순서**: Was → Infra (역순)

## 인증 시스템

### 자동 인증 (Hooks)

**시나리오마다 자동 실행**:
```java
@Before
public void setUp() {
    CommonContextHolder context = CommonContextHolder.getInstance();
    context.setRequestSpec(RequestSpecFactory.create());

    // 자동 admin 로그인
    String adminToken = BaseApiHelper.authenticateAndGetToken();
    context.setAdminToken(adminToken);
}
```

**인증 API**:
```java
// BaseApiHelper.authenticateAndGetToken() 내부
POST http://localhost:8082/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin123"
}

// 응답
Set-Cookie: AUTH_TOKEN=xxxxx
```

### 인증 사용

**Helper에서 토큰 사용**:
```java
public static Response createCamping(String name) {
    String adminToken = CommonContextHolder.getInstance().getAdminToken();

    return given()
            .cookie("AUTH_TOKEN", adminToken)  // 쿠키로 전달
            .contentType(ContentType.JSON)
            .body(Map.of("name", name))
            .post(ADMIN_BASE_URL + "/api/campings");
}
```

**토큰 없이 요청** (인증 불필요한 엔드포인트):
```java
public static Response getHealthCheck() {
    return given()
            .when()
            .get(ADMIN_BASE_URL + "/");  // 토큰 없음
}
```

## 데이터 시딩

### DB 초기 데이터

**위치**: `infra/db/init.sql`

**시딩 시점**: MySQL 컨테이너 최초 시작 시 자동 실행

**포함 데이터**:
```sql
-- 1. 테이블 생성
CREATE TABLE IF NOT EXISTS products (...)
CREATE TABLE IF NOT EXISTS campsites (...)
CREATE TABLE IF NOT EXISTS reservations (...)
CREATE TABLE IF NOT EXISTS sales_records (...)
CREATE TABLE IF NOT EXISTS rental_records (...)

-- 2. 기본 데이터 INSERT
-- Products: 랜턴, 장작팩, 코펠, 의자 등 12개
-- Campsites: A-1 ~ A-20, B-1 ~ B-15 (35개)
-- Reservations: 오늘/미래/과거 예약 데이터 (confirmation_code로 중복 방지)
-- Sales/Rental Records: 거래 내역
```

**중복 방지**:
```sql
ON DUPLICATE KEY UPDATE ...
```
- `products`: `name` 유니크
- `campsites`: `site_number` 유니크
- `reservations`: `confirmation_code` 유니크

### WireMock 스텁 시딩

**위치**: `infra/wiremock/mappings/*.json`

**로딩 시점**: payments-mock 컨테이너 시작 시 볼륨 마운트로 자동 로드

**예시** (`payment-confirm-success.json`):
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/payments/confirm",
    "bodyPatterns": [
      {
        "matchesJsonPath": "$.amount",
        "equalToJson": "10000"
      }
    ]
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "success": true,
      "transactionId": "{{request.body.paymentKey}}"
    }
  }
}
```

**스텁 검증**:
```bash
./gradlew verifyWiremockStubs
```

## 환경 변수

### System Properties (build.gradle.kts)

```kotlin
tasks.test {
    systemProperty("KIOSK_BASE_URL", "http://localhost:8081")
    systemProperty("ADMIN_BASE_URL", "http://localhost:8082")
    systemProperty("RESERVATION_BASE_URL", "http://localhost:8083")
    systemProperty("PAYMENTS_BASE_URL", "http://localhost:8084")
}
```

**사용법**:
```java
private static final String KIOSK_BASE_URL = System.getProperty("KIOSK_BASE_URL");
```

### Docker 환경 변수 (docker-compose.yml)

**MySQL 연결** (admin/reservation):
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://atdd-db:3306/atdd?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  - SPRING_DATASOURCE_USERNAME=root
  - SPRING_DATASOURCE_PASSWORD=secret
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
  - SPRING_JPA_HIBERNATE_DDL_AUTO=none
  - SPRING_SQL_INIT_MODE=never
```

**결제 게이트웨이** (kiosk):
```yaml
environment:
  - PAYMENTS_SECRET_KEY=test_sk_dummy
  - PAYMENTS_BASE_URL=http://payments-mock:8080
```

## 네트워크

### Docker 네트워크: `atdd-net`

**생성**: `docker-compose-infra.yml`에서 생성
```yaml
networks:
  atdd-net:
    name: atdd-net
    external: false  # 자동 생성
```

**공유**: 모든 서비스가 동일 네트워크 사용
```yaml
services:
  admin:
    networks:
      - atdd-net
  kiosk:
    networks:
      - atdd-net
```

**컨테이너 간 통신**:
- kiosk → admin: `http://admin:8080` (컨테이너명)
- kiosk → payments-mock: `http://payments-mock:8080`
- 서비스 → DB: `jdbc:mysql://atdd-db:3306/atdd`

**호스트 → 컨테이너**:
- kiosk: `http://localhost:8081`
- admin: `http://localhost:8082`
- DB: `localhost:3306`

## 테스트 라이프사이클

### 시나리오 실행 흐름

```
1. @Before 훅
   ├─ RequestSpec 초기화
   └─ Admin 자동 로그인 (AUTH_TOKEN 저장)

2. Given/When/Then 스텝
   ├─ Helper로 API 호출
   ├─ Response를 CommonContextHolder에 저장
   └─ 검증

3. @After 훅
   └─ CommonContextHolder.clear() (ThreadLocal 정리)
```

### Context 관리

```java
// @Before: 초기화
CommonContextHolder ctx = CommonContextHolder.getInstance();
ctx.setRequestSpec(...);
ctx.setAdminToken(...);

// Step: 데이터 저장/조회
ctx.setResponse(response);
Response response = ctx.getResponse();

// @After: 정리
CommonContextHolder.clear();
```

## 트러블슈팅

### 서비스 상태 확인

```bash
# 컨테이너 상태
docker ps

# 특정 서비스 로그
docker logs admin
docker logs kiosk
docker logs atdd-db
docker logs payments-mock

# 헬스체크 확인
curl http://localhost:8082/  # admin
curl http://localhost:8081/  # kiosk
curl http://localhost:8084/__admin/mappings  # wiremock
```

### 인증 실패

**증상**: `AUTH_TOKEN is null` 또는 401 에러

**확인사항**:
1. Admin 서비스 정상 실행 여부: `docker ps | grep admin`
2. 로그인 엔드포인트 응답: `curl -X POST http://localhost:8082/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'`
3. Hooks가 정상 실행되는지 확인

### DB 연결 실패

**증상**: `Could not connect to database`

**확인사항**:
1. MySQL 컨테이너 헬스: `docker ps | grep atdd-db`
2. 연결 테스트: `docker exec -it atdd-db mysql -uroot -psecret -e "SHOW DATABASES;"`
3. 네트워크: `docker network inspect atdd-net`

### WireMock 스텁 미동작

**증상**: 결제 확정 시 예상과 다른 응답

**확인사항**:
1. 스텁 로드 확인: `./gradlew verifyWiremockStubs`
2. 매핑 조회: `curl http://localhost:8084/__admin/mappings`
3. 요청 로그: `docker logs payments-mock`

### 서비스 재시작

```bash
# 전체 재시작
./gradlew ComposeAllDown
./gradlew ComposeAllUp

# 특정 서비스만 재시작
docker-compose -f infra/docker-compose.yml restart kiosk
```

## AI 작성 시 체크리스트

### 환경 설정
- [ ] 초기 설정: `./gradlew SetupAllServices` 실행됨
- [ ] 서비스 시작: `./gradlew ComposeAllUp` 실행됨
- [ ] 헬스체크: 모든 서비스가 healthy 상태

### 인증
- [ ] @Before 훅에서 자동 인증됨 (수동 로그인 불필요)
- [ ] Helper에서 `CommonContextHolder.getInstance().getAdminToken()` 사용
- [ ] 인증 필요 API는 `.cookie("AUTH_TOKEN", token)` 추가

### 데이터
- [ ] DB 초기 데이터는 `init.sql`에서 자동 시딩됨
- [ ] 테스트 데이터는 Feature에서 파라미터로 전달
- [ ] WireMock 스텁은 `infra/wiremock/mappings/` 에 JSON 파일로 추가

### 실행
- [ ] Base URL은 System Property 사용
- [ ] Response는 CommonContextHolder에 저장
- [ ] 테스트 종료 후 `./gradlew ComposeAllDown` 실행 권장