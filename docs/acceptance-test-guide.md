# 📚 ATDD 캠핑 예약 시스템 인수테스트 가이드

## 📋 개요

캠핑 예약 시스템의 ATDD(Acceptance Test-Driven Development) 인수테스트 작성을 위한 실전 가이드입니다. **순서대로 따라하면서 배우는 방식**으로 구성되어 있습니다.

## 🎯 인수테스트 작성 4단계

### 1단계: Feature 파일 작성 (Gherkin)
### 2단계: Step Definition 작성 (Java)
### 3단계: TestFixture 작성 (API 호출 & 검증)
### 4단계: 테스트 실행 및 확인

---

## 🚀 1단계: Feature 파일 작성

### 📍 위치
```
src/test/resources/features/
├── integration/             # 통합 시나리오 (2개 이상 서비스 연동)
│   ├── normal-integration.feature    # 정상 통합 시나리오
│   ├── boundary-integration.feature  # 경계 통합 시나리오
│   └── exception-integration.feature # 예외 통합 시나리오
├── e2e.feature              # 통합 테스트
├── payment-e2e.feature      # 결제 E2E 테스트
└── smoke.feature            # 스모크 테스트
```

### 📝 기본 구조

```gherkin
Feature: 기능 설명

  Scenario: 시나리오 이름
    Given 전제조건
    When 실행 동작
    Then 예상 결과
    And 추가 검증
```

### ✅ 실제 예시 1: 상품 조회

**파일**: `src/test/resources/features/product.feature`
```gherkin
Feature: 상품 관리

  Scenario: 키오스크에서 상품 목록을 조회할 수 있다
    When 회원은 키오스크에서 상품 목록을 조회한다
    Then 상품 목록이 1개 이상 나온다
    And 상품에는 이름, 가격, 수량, 타입이 있다
```

### ✅ 실제 예시 2: 결제 테스트

**파일**: `src/test/resources/features/payment.feature`
```gherkin
Feature: 결제 기능

  Scenario: 결제 성공 - 정상적인 금액으로 결제 요청
    Given 상품 목록에서 결제할 상품을 선택한다
      | productId | quantity | price |
      | 1         | 2        | 5000  |
    When 정상 금액으로 결제를 요청한다
    Then 결제가 성공한다
    And 결제 응답에 paymentKey가 포함되어 있다

  Scenario: 결제 실패 - 유효하지 않은 금액
    Given 상품 목록에서 결제할 상품을 선택한다
      | productId | quantity | price |
      | 1         | 2        | 5000  |
    When 유효하지 않은 금액으로 결제를 요청한다
    Then 결제가 실패한다
    And 실패 메시지가 "결제 생성 실패"이다
```

### 🤖 AI Assistant가 생성한 테스트 예시

**AI가 생성하는 모든 Feature 파일에는 `@ai-assistant` 태그를 반드시 포함해야 합니다:**

**파일**: `src/test/resources/features/ai-generated-test.feature`
```gherkin
@ai-assistant
Feature: AI가 생성한 새로운 기능 테스트

  @ai-assistant
  Scenario: AI가 작성한 테스트 시나리오
    Given AI가 전제조건을 설정한다
    When AI가 기능을 실행한다
    Then AI가 예상 결과를 확인한다
```

**태그 활용 예시:**
```bash
# AI가 생성한 테스트만 실행
./gradlew test -Dcucumber.filter.tags="@ai-assistant"

# AI가 생성한 테스트 제외하고 실행
./gradlew test -Dcucumber.filter.tags="not @ai-assistant"
```

---

## 🚀 2단계: Step Definition 작성

### 📍 위치
```
src/test/java/com/camping/tests/steps/
├── IntegrationSteps.java    # 통합 테스트 스텝
├── PaymentSteps.java        # 결제 테스트 스텝
└── SmokeSteps.java         # 스모크 테스트 스텝
```

### 📝 기본 구조

```java
public class ExampleSteps {
    private ExtractableResponse<Response> response;

    @Given("전제조건 설명")
    public void 전제조건_메서드() {
        // TestFixture 메서드 호출
    }

    @When("실행 동작 설명")
    public void 실행_메서드() {
        // TestFixture 메서드 호출
        response = TestFixture.메서드();
    }

    @Then("예상 결과 설명")
    public void 검증_메서드() {
        // TestFixture 검증 메서드 호출
        TestFixture.검증메서드(response);
    }
}
```

### ✅ 실제 예시 1: 상품 조회 스텝

**파일**: `src/test/java/com/camping/tests/steps/ProductSteps.java`
```java
public class ProductSteps {
    private ExtractableResponse<Response> response;

    @When("회원은 키오스크에서 상품 목록을 조회한다.")
    public void 회원은키오스크에서상품목록을조회한다() {
        response = 키오스크_상품_목록_조회();  // TestFixture 메서드
    }

    @Then("상품 목록이 {int}개 이상 나온다.")
    public void 상품목록이개이상나온다(int quantity) {
        상품_목록_개수_검증(response, quantity);  // TestFixture 검증 메서드
    }

    @And("상품에는 이름, 가격, 수량, 타입이 있다.")
    public void 상품에는이름가격수량타입이있다() {
        상품_기본_필드_검증(response);  // TestFixture 검증 메서드
    }
}
```

### ✅ 실제 예시 2: 결제 스텝

**파일**: `src/test/java/com/camping/tests/steps/PaymentSteps.java`
```java
public class PaymentSteps {
    private List<Map<String, Object>> selectedItems = new ArrayList<>();
    private ExtractableResponse<Response> paymentResponse;

    @Given("상품 목록에서 결제할 상품을 선택한다")
    public void 상품목록에서결제할상품을선택한다(DataTable dataTable) {
        List<Map<String, String>> items = dataTable.asMaps();
        selectedItems = 상품_목록_생성(items);  // TestFixture 메서드
    }

    @When("정상 금액으로 결제를 요청한다")
    public void 정상금액으로결제를요청한다() {
        paymentResponse = 정상_금액으로_결제_요청(selectedItems);  // TestFixture 메서드
    }

    @Then("결제가 성공한다")
    public void 결제가성공한다() {
        결제_성공_검증(paymentResponse);  // TestFixture 검증 메서드
    }

    @And("결제 응답에 paymentKey가 포함되어 있다")
    public void 결제응답에paymentKey가포함되어있다() {
        paymentKey_포함_검증(paymentResponse);  // TestFixture 검증 메서드
    }
}
```

---

## 🚀 3단계: TestFixture 작성

### 📍 위치
```
src/test/java/com/camping/tests/support/fixture/
├── KioskTestFixture.java      # 키오스크 테스트 픽스처
└── PaymentTestFixture.java    # 결제 테스트 픽스처
```

### 📝 기본 구조

```java
public class ExampleTestFixture {

    // API 호출 메서드 (빌더 패턴 사용)
    public static ExtractableResponse<Response> API_호출_메서드() {
        ExtractableResponse<Response> response = ApiClientFactory.serviceType()
            .httpMethod("/api/endpoint")
            .body(requestBody)
            .needAuth()  // 인증이 필요한 경우
            .execute();
        assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
        return response;
    }

    // 검증 메서드
    public static void 검증_메서드(ExtractableResponse<Response> response) {
        assertThat(response.jsonPath().get("field")).isEqualTo(expectedValue);
    }
}
```

### ✅ 실제 예시 1: 키오스크 TestFixture

**파일**: `src/test/java/com/camping/tests/support/fixture/KioskTestFixture.java`
```java
public class KioskTestFixture {

    // API 호출 메서드
    public static ExtractableResponse<Response> 키오스크_상품_목록_조회() {
        ExtractableResponse<Response> response = ApiClientFactory.kiosk()
            .get("/api/products")
            .needAuth()  // 인증 필요
            .execute();
        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 검증 메서드들
    public static void 상품_목록_개수_검증(ExtractableResponse<Response> response, int expectedMinCount) {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(expectedMinCount);
    }

    public static void 상품_기본_필드_검증(ExtractableResponse<Response> response) {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products).isNotEmpty();

        for (Map<String, Object> product : products) {
            assertThat(product.get("name")).as("상품 이름").isNotNull();
            assertThat(product.get("price")).as("상품 가격").isNotNull();
            assertThat(product.get("stockQuantity")).as("상품 수량").isNotNull();
            assertThat(product.get("productType")).as("상품 타입").isNotNull();
        }
    }
}
```

### ✅ 실제 예시 2: 결제 TestFixture

**파일**: `src/test/java/com/camping/tests/support/fixture/PaymentTestFixture.java`
```java
public class PaymentTestFixture {

    // API 호출 메서드
    public static ExtractableResponse<Response> 정상_금액으로_결제_요청(List<Map<String, Object>> selectedItems) {
        // 요청 데이터 구성
        List<Map<String, Object>> cartItems = new ArrayList<>();
        for (Map<String, Object> item : selectedItems) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", item.get("productId"));
            cartItem.put("productName", "Test Product " + item.get("productId"));
            cartItem.put("unitPrice", item.get("price"));
            cartItem.put("quantity", item.get("quantity"));
            cartItems.add(cartItem);
        }

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("items", cartItems);
        paymentRequest.put("paymentMethod", "CARD");

        // API 호출 (결제는 인증 불필요)
        return ApiClientFactory.kiosk()
            .post("/api/payments")
            .body(paymentRequest)
            .execute();
    }

    public static ExtractableResponse<Response> 유효하지_않은_금액으로_결제_요청() {
        Map<String, Object> paymentRequest = Map.of(
            "amount", 0,  // 0원으로 설정하여 에러 유발
            "paymentMethod", "CARD"
        );

        return ApiClientFactory.kiosk()
            .post("/api/payments")
            .body(paymentRequest)
            .execute();
    }

    // 검증 메서드들
    public static void 결제_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }

    public static void 결제_실패_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getBoolean("success")).isFalse();
    }

    public static void paymentKey_포함_검증(ExtractableResponse<Response> response) {
        String paymentKey = response.jsonPath().getString("paymentKey");
        assertThat(paymentKey).isNotNull();
        assertThat(paymentKey).isNotEmpty();
    }

    public static void 실패_메시지_검증(ExtractableResponse<Response> response, String expectedMessage) {
        String actualMessage = response.jsonPath().getString("message");
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    // 데이터 생성 메서드
    public static List<Map<String, Object>> 상품_목록_생성(List<Map<String, String>> items) {
        List<Map<String, Object>> selectedItems = new ArrayList<>();
        for (Map<String, String> item : items) {
            Map<String, Object> selectedItem = new HashMap<>();
            selectedItem.put("productId", Long.parseLong(item.get("productId")));
            selectedItem.put("quantity", Integer.parseInt(item.get("quantity")));
            selectedItem.put("price", Integer.parseInt(item.get("price")));
            selectedItems.add(selectedItem);
        }
        return selectedItems;
    }
}
```

---

## 🚀 4단계: 테스트 실행

### ⚙️ Hooks 설정

**파일**: `src/test/java/com/camping/tests/steps/Hooks.java`
```java
public class Hooks {

    @BeforeAll
    public static void initAccessToken() {
        // Admin 로그인으로 JWT 토큰 획득
        Map<String, String> params = Map.of("username", "admin", "password", "admin123");
        String adminAccessToken = requestAdminLogin(params);

        // 각 서비스에 토큰 설정
        ServiceContext.setAccessToken(ServiceType.ADMIN, adminAccessToken);
        ServiceContext.setAccessToken(ServiceType.KIOSK, adminAccessToken);  // Kiosk도 Admin 토큰 사용
    }

    @Before
    public void beforeScenario() {
        // 각 시나리오 실행 전 RequestSpec 초기화
        ServiceContext.initializeRequestSpec(ServiceType.ADMIN);
        ServiceContext.initializeRequestSpec(ServiceType.KIOSK);
        ServiceContext.initializeRequestSpec(ServiceType.RESERVATION);
    }
}
```

### 🏃‍♂️ 실행 명령어

```bash
# 전체 테스트 실행
./gradlew test

# 스모크 테스트 (인프라 포함)
./gradlew smokeTest

# 특정 태그 테스트
./gradlew test -Dcucumber.filter.tags="@payment"

# AI가 생성한 테스트만 실행
./gradlew test -Dcucumber.filter.tags="@ai-assistant"

# AI가 생성한 테스트 제외하고 실행
./gradlew test -Dcucumber.filter.tags="not @ai-assistant"

# 복합 태그 사용 (결제 테스트 중 AI가 생성한 것만)
./gradlew test -Dcucumber.filter.tags="@payment and @ai-assistant"
```

---

## 💡 인증 처리 가이드

### 🔐 언제 인증이 필요한가?

| 서비스 | 엔드포인트 | 인증 필요 | needAuth | 비고 |
|--------|------------|-----------|----------|------|
| **Admin** | `/admin/*` | ✅ | `true` | 모든 관리 API |
| **Kiosk** | `/api/products` | ✅ | `true` | Admin 연동 필요 |
| **Kiosk** | `/api/payments` | ❌ | `false` | 결제 API |
| **Reservation** | `/api/reservations` | ❌ | `false` | 고객용 API |

### 📝 인증 사용 예시

```java
// ✅ 인증 필요한 API
ApiClientFactory.admin().get("/admin/products").needAuth().execute();      // Admin API
ApiClientFactory.kiosk().get("/api/products").needAuth().execute();        // Kiosk 상품조회

// 🔓 인증 불필요한 API
ApiClientFactory.kiosk().post("/api/payments").body(data).execute();       // 결제 API
ApiClientFactory.reservation().post("/api/reservations").body(data).execute(); // 예약 API
```

---

## 🛠️ API 클라이언트 사용법

### 🏭 ApiClientFactory 사용

```java
// 서비스별 클라이언트 생성
ApiClient adminClient = ApiClientFactory.admin();        // Admin 서비스
ApiClient kioskClient = ApiClientFactory.kiosk();        // Kiosk 서비스
ApiClient reservationClient = ApiClientFactory.reservation(); // Reservation 서비스

// 메서드 체이닝 방식 (빌더 패턴)
ExtractableResponse<Response> response = ApiClientFactory.admin()
    .get("/admin/products")
    .needAuth()  // 인증 필요
    .execute();
```

### 🔄 HTTP 메서드 사용

```java
// GET 요청
ApiClientFactory.kiosk().get("/api/products").needAuth().execute();

// POST 요청
ApiClientFactory.admin().post("/admin/products").body(productData).needAuth().execute();

// PUT 요청
ApiClientFactory.admin().put("/admin/products/1").body(updateData).needAuth().execute();

// PATCH 요청
ApiClientFactory.admin().patch("/admin/reservations/1/status").body(statusData).needAuth().execute();

// DELETE 요청 (인증 불필요한 경우)
ApiClientFactory.reservation().delete("/api/reservations/1?confirmationCode=ABC").execute();
```

---

## 📊 완전한 인수테스트 예시

### 🎯 전체 플로우: 상품 조회 → 결제

**Feature 파일:**
```gherkin
Feature: 키오스크 주문 플로우

  Scenario: 고객이 키오스크에서 상품을 주문한다
    When 고객이 키오스크에서 상품 목록을 조회한다
    Then 상품 목록이 조회된다
    When 고객이 상품을 선택하고 결제한다
    Then 결제가 성공한다
```

**Step Definition:**
```java
public class OrderSteps {
    private ExtractableResponse<Response> productResponse;
    private ExtractableResponse<Response> paymentResponse;

    @When("고객이 키오스크에서 상품 목록을 조회한다")
    public void 고객이키오스크에서상품목록을조회한다() {
        productResponse = 키오스크_상품_목록_조회();
    }

    @Then("상품 목록이 조회된다")
    public void 상품목록이조회된다() {
        상품_목록_개수_검증(productResponse, 1);
    }

    @When("고객이 상품을 선택하고 결제한다")
    public void 고객이상품을선택하고결제한다() {
        // 상품 선택
        List<Map<String, Object>> selectedItems = List.of(
            Map.of("productId", 1L, "quantity", 2, "price", 5000)
        );

        // 결제 요청
        paymentResponse = 정상_금액으로_결제_요청(selectedItems);
    }

    @Then("결제가 성공한다")
    public void 결제가성공한다() {
        결제_성공_검증(paymentResponse);
        paymentKey_포함_검증(paymentResponse);
    }
}
```

---

## 🚨 자주 발생하는 문제 해결

### 1. 401 Unauthorized
```
원인: 인증이 필요한 API에 토큰이 없음
해결: needAuth=true 설정 확인
```

### 2. Feature 파일을 찾을 수 없음
```
원인: src/test/resources/features/ 경로 확인
해결: 경로와 파일명 정확성 체크
```

### 3. Step Definition 매칭 안됨
```
원인: 메서드명과 Gherkin 스텝 불일치
해결: 정확한 문자열 매칭 확인
```

---

## 🎯 인수테스트 체크리스트

### ✅ Feature 파일
- [ ] 비즈니스 언어로 작성되었는가?
- [ ] Given-When-Then 구조를 따르는가?
- [ ] 사용자 관점에서 이해 가능한가?
- [ ] AI가 생성한 경우 `@ai-assistant` 태그를 포함했는가?

### ✅ Step Definition
- [ ] 각 스텝이 하나의 책임만 가지는가?
- [ ] TestFixture 메서드로 위임하고 있는가?
- [ ] 파라미터화가 적절히 사용되었는가?

### ✅ TestFixture
- [ ] 의미 있는 메서드명을 사용하는가?
- [ ] 적절한 서비스 클라이언트를 사용하는가?
- [ ] 인증이 필요한 경우 needAuth=true 설정했는가?
- [ ] 응답 검증이 포함되어 있는가?

### 🤖 AI 생성 콘텐츠
- [ ] Feature 파일에 `@ai-assistant` 태그가 포함되어 있는가?
- [ ] Scenario에도 `@ai-assistant` 태그가 포함되어 있는가?
- [ ] AI 생성 테스트와 수동 작성 테스트를 구분할 수 있는가?

---

## 📚 문서 참고

- 🔐 **[authentication-guide.md](./authentication-guide.md)** - 상세 인증 가이드
- 🛠️ **[helper-system.md](./helper-system.md)** - API 클라이언트 시스템
- 🎭 **[wiremock-guide.md](./wiremock-guide.md)** - WireMock 모킹 가이드

이 가이드를 순서대로 따라하면서 안정적이고 유지보수하기 쉬운 인수테스트를 작성하세요! 🎉