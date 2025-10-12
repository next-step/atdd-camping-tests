# 인수 테스트 작성 가이드

> AI(Claude)가 테스트 코드를 작성할 때 참조하는 가이드
>
> **환경 설정/실행은 `auth-setup.md` 참조**

## 시스템 개요

### 서비스 아키텍처
```
kiosk (8081) ──> reservation (8083)  [예약 데이터 조회]
             ──> admin (8082)        [관리자 데이터 조회]
             ──> payments-mock (8084) [결제 게이트웨이, WireMock]

All services ──> atdd-db (3306)      [공유 MySQL]
```

**테스트 플로우**: `.feature` → Step Definitions → API Helpers → RestAssured → Docker 서비스

### 핵심 엔드포인트

#### Kiosk Service (8081)
```
POST /api/payments              # 결제 생성
POST /api/payments/confirm      # 결제 확정 (→ payments-mock 호출)
GET  /api/campings              # 캠핑장 목록 (← admin 조회)
```

#### Admin Service (8082)
```
POST /auth/login                # 인증 (AUTH_TOKEN 쿠키 반환)
POST /api/campings              # 캠핑장 생성 (AUTH_TOKEN 필수)
GET  /api/campings              # 캠핑장 목록
```

#### Payments Mock (8084)
```
POST /payments/confirm
  - amount: 10000 → success: true
  - amount: 12345 → success: false
```

## 테스트 컴포넌트

### CommonContextHolder

**목적**: 스텝 간 데이터 공유 (ThreadLocal 기반)

**라이프사이클**:
- `@Before` 훅: RequestSpec + Admin 토큰 자동 초기화
- 시나리오 실행: 데이터 저장/조회
- `@After` 훅: 컨텍스트 정리

**사용법**:
```java
CommonContextHolder ctx = CommonContextHolder.getInstance();

// Response 저장/조회
ctx.setResponse(response);
Response response = ctx.getResponse();

// Admin 토큰 조회 (@Before 훅에서 자동 초기화됨)
String token = ctx.getAdminToken();
```

### 인증

**자동 인증**: 모든 시나리오에서 `@Before` 훅이 자동으로 admin 토큰 초기화

```java
// Helper에서 토큰 사용
String token = CommonContextHolder.getInstance().getAdminToken();

given()
    .cookie("AUTH_TOKEN", token)
    .post(ADMIN_BASE_URL + "/api/campings");
```

## 코드 작성 패턴

### ✅ 올바른 Step Definition

```java
package com.camping.tests.steps;

import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.helpers.KioskPaymentApiHelper;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentSteps {
    private String paymentKey;  // 시나리오 스코프 데이터
    private String orderId;

    private CommonContextHolder context() {
        return CommonContextHolder.getInstance();
    }

    @When("키오스크에 결제 생성을 요청한다")
    public void requestPaymentCreation() {
        // 1. Helper로 API 호출
        Response response = KioskPaymentApiHelper.createPayment();

        // 2. 응답 데이터 추출
        if (response.getStatusCode() == 200) {
            paymentKey = response.jsonPath().getString("paymentKey");
            orderId = response.jsonPath().getString("orderId");
        }

        // 3. Context에 저장 (다른 스텝에서 사용)
        context().setResponse(response);
    }

    @Then("결제가 성공이어야 한다")
    public void verifyPaymentSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);

        boolean success = response.jsonPath().getBoolean("success");
        assertThat(success).isTrue();
    }
}
```

**핵심 원칙**:
- Helper 클래스로 API 호출 (Step에서 직접 호출 금지)
- 인스턴스 변수로 시나리오 데이터 관리
- `CommonContextHolder`로 스텝 간 데이터 공유
- 상태 코드 + 비즈니스 로직 모두 검증

### ✅ 올바른 API Helper

```java
package com.camping.tests.helpers;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.List;

public class KioskPaymentApiHelper {
    private static final String KIOSK_BASE_URL = System.getProperty("KIOSK_BASE_URL");

    public static Response createPayment() {
        Map<String, Object> requestBody = Map.of(
            "items", List.of(
                Map.of(
                    "productId", 1,
                    "productName", "테스트 상품",
                    "unitPrice", 10000,
                    "quantity", 1
                )
            ),
            "paymentMethod", "CARD"
        );

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments");
    }

    public static Response confirmPayment(String paymentKey, String orderId, int amount) {
        Map<String, Object> requestBody = Map.of(
            "paymentKey", paymentKey,
            "orderId", orderId,
            "amount", amount
        );

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments/confirm");
    }
}
```

**핵심 원칙**:
- Static 메서드 (stateless)
- System Property로 Base URL 관리
- 파라미터화로 재사용성
- Response 반환 (호출자가 처리)

### ✅ 올바른 Feature 파일

```gherkin
Feature: 키오스크 결제 E2E (WireMock)

  Scenario: 결제 승인 성공
    When 키오스크에 결제 생성을 요청한다
    And 키오스크에 결제 확정을 요청한다
    Then 결제가 성공이어야 한다

  Scenario: 결제 승인 실패 - 잘못된 금액
    When 키오스크에 결제 생성을 요청한다
    And 키오스크에 금액 "12345"원으로 결제 확정을 요청한다
    Then 결제가 실패이어야 한다
```

**핵심 원칙**:
- 비즈니스 언어 사용 (기술 세부사항 X)
- 명확한 시나리오 이름
- 한국어 스텝 (프로젝트 표준)
- 파라미터는 따옴표로 표시

### ❌ 안티패턴

```java
// ❌ Step에서 직접 API 호출
@When("결제를 요청한다")
public void requestPayment() {
    given().post("http://localhost:8081/api/payments"); // Helper 미사용, URL 하드코딩
}

// ❌ 검증 누락
@Then("성공한다")
public void verify() {
    // 아무 검증도 없음
}

// ❌ Context 미사용
@When("결제를 생성한다")
public void createPayment() {
    Response response = helper.create();
    // Context에 저장 안 함 - 다음 스텝에서 사용 불가!
}

// ❌ Feature에 기술 세부사항
Scenario: API 테스트
  When POST /api/payments with body {"amount": 10000}
  Then status code is 200
```

## WireMock 스텁 작성

**위치**: `infra/wiremock/mappings/{stub-name}.json`

**예시**:
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

**매칭 전략**:
- `urlPath`: 정확한 경로 매칭
- `bodyPatterns.matchesJsonPath`: JSONPath 조건
- `bodyPatterns.equalToJson`: 값 일치

**응답 템플릿**:
- `{{request.body.fieldName}}`: 요청 필드 에코
- `{{randomValue type='UUID'}}`: 랜덤 UUID

## 작성 예시

### 예시 1: 다중 서비스 E2E

**Feature**:
```gherkin
Feature: 예약 생성 및 조회 E2E

  Scenario: 관리자가 캠핑장을 생성하고 키오스크에서 조회한다
    Given 관리자가 "한강 캠핑장" 캠핑장을 생성한다
    When 키오스크에서 캠핑장 목록을 조회한다
    Then 캠핑장 목록에 "한강 캠핑장"이 포함되어 있다
```

**Step Definitions**:
```java
package com.camping.tests.steps;

import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.helpers.AdminApiHelper;
import com.camping.tests.helpers.KioskApiHelper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class CampingE2ESteps {
    private Long campingId;

    @Given("관리자가 {string} 캠핑장을 생성한다")
    public void createCamping(String campingName) {
        Response response = AdminApiHelper.createCamping(campingName);
        campingId = response.jsonPath().getLong("id");
        CommonContextHolder.getInstance().setResponse(response);
    }

    @When("키오스크에서 캠핑장 목록을 조회한다")
    public void getCampingList() {
        Response response = KioskApiHelper.getCampingList();
        CommonContextHolder.getInstance().setResponse(response);
    }

    @Then("캠핑장 목록에 {string}이 포함되어 있다")
    public void verifyCampingInList(String campingName) {
        Response response = CommonContextHolder.getInstance().getResponse();

        List<String> campingNames = response.jsonPath()
                .getList("data.name", String.class);

        assertThat(campingNames).contains(campingName);
    }
}
```

**Helpers**:
```java
// AdminApiHelper.java
public class AdminApiHelper {
    private static final String ADMIN_BASE_URL = System.getProperty("ADMIN_BASE_URL");

    public static Response createCamping(String campingName) {
        String adminToken = CommonContextHolder.getInstance().getAdminToken();

        return given()
                .cookie("AUTH_TOKEN", adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", campingName))
                .when()
                .post(ADMIN_BASE_URL + "/api/campings");
    }
}

// KioskApiHelper.java
public class KioskApiHelper {
    private static final String KIOSK_BASE_URL = System.getProperty("KIOSK_BASE_URL");

    public static Response getCampingList() {
        return given()
                .when()
                .get(KIOSK_BASE_URL + "/api/campings");
    }
}

// ReservationApiHelper.java
public class ReservationApiHelper {
    private static final String RESERVATION_BASE_URL = System.getProperty("RESERVATION_BASE_URL");

    public static Response createReservation(String campingId, String startDate, String endDate) {
        Map<String, Object> requestBody = Map.of(
            "campingId", campingId,
            "startDate", startDate,
            "endDate", endDate
        );

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(RESERVATION_BASE_URL + "/api/reservations");
    }

    public static Response getReservation(String reservationId) {
        return given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);
    }
}
```

## 데이터 격리 전략

### 시나리오 간 격리

**원칙**: 각 시나리오는 독립적으로 실행 가능해야 함

**전략**:
1. **유니크 데이터 생성**: 타임스탬프, UUID 활용
2. **테스트 후 정리**: DB 초기 상태로 복원 (필요 시)
3. **격리된 컨텍스트**: `CommonContextHolder`는 시나리오마다 초기화됨

**예시**:
```java
@Given("고유한 캠핑장을 생성한다")
public void createUniqueCamping() {
    String uniqueName = "캠핑장_" + System.currentTimeMillis();
    Response response = AdminApiHelper.createCamping(uniqueName);

    campingId = response.jsonPath().getLong("id");
    CommonContextHolder.getInstance().setResponse(response);
}
```

### 병렬 실행 대비

**현재 제약**: ThreadLocal 기반 컨텍스트는 순차 실행만 지원

**향후 병렬 실행 시 고려사항**:
- Scenario 레벨에서 `@Serial` 태그 사용
- 또는 시나리오별 독립 DB 스키마 사용

### DB 상태 관리

**기본 원칙**: `init.sql`로 시딩된 데이터는 읽기 전용으로 취급

**테스트 데이터 생성**:
```java
// ✅ 올바름: 테스트에서 새로운 데이터 생성
@Given("관리자가 {string} 캠핑장을 생성한다")
public void createCamping(String name) {
    Response response = AdminApiHelper.createCamping(name);
    // 생성된 데이터는 시나리오 종료 후 남아있을 수 있음
}

// ❌ 피할 것: init.sql 데이터에 의존
@Given("캠핑장 ID 1을 조회한다")
public void getCampingById() {
    // ID 1이 항상 존재한다고 가정하면 불안정
}
```

**정리가 필요한 경우**:
```java
// @After 훅에서 테스트 데이터 삭제 (선택적)
@After
public void cleanupTestData() {
    if (createdCampingId != null) {
        AdminApiHelper.deleteCamping(createdCampingId);
    }
}
```

## AI 작성 가이드라인

### 필수 규칙
1. API 호출은 반드시 Helper 클래스 사용
2. Response는 반드시 `CommonContextHolder`에 저장
3. 상태 코드 + 비즈니스 로직 모두 검증
4. Base URL은 System Property 사용
5. Gherkin 스텝은 한국어 사용

### 권장 사항
- 파라미터화된 스텝 작성 (하드코딩 지양)
- Feature는 비즈니스 언어 사용
- 테스트 데이터는 Feature에서 파라미터로 전달

### 코드 품질 체크리스트
- [ ] Helper 클래스 생성/재사용
- [ ] Response를 CommonContextHolder에 저장
- [ ] 상태 코드 검증
- [ ] AssertJ 비즈니스 검증
- [ ] System Property 사용
- [ ] 한국어 스텝 정의
- [ ] 테스트 데이터 파라미터화
- [ ] WireMock 스텁 추가 (외부 API)

## 빠른 참조

### RestAssured
```java
// GET
given().when().get(baseUrl + "/api/resource")

// POST
given()
    .contentType(ContentType.JSON)
    .body(requestBody)
    .when()
    .post(baseUrl + "/api/resource")

// 인증
given()
    .cookie("AUTH_TOKEN", token)
    .when()
    .post(baseUrl + "/api/resource")

// 값 추출
String value = response.jsonPath().getString("field");
List<String> list = response.jsonPath().getList("field", String.class);
```

### AssertJ
```java
assertThat(actual).isEqualTo(expected);
assertThat(actual).isTrue();
assertThat(list).contains(element);
assertThat(list).hasSize(3);
```

### Cucumber
```java
@Given("관리자가 {string}을 생성한다")
public void create(String name) { }

@When("사용자가 {int}개 요청한다")
public void request(int count) { }
```

## 디버깅 팁

### RestAssured 로깅
```java
// 요청/응답 전체 로그
given()
    .log().all()  // 요청 로그
    .when()
    .get(baseUrl + "/api/resource")
    .then()
    .log().all()  // 응답 로그
```

### 빠른 테스트 실행
```bash
# 모든 테스트
./gradlew test

# 특정 태그만
./gradlew test -Dcucumber.filter.tags="@smoke"
```

**환경 문제 해결은 `auth-setup.md` 트러블슈팅 섹션 참조**