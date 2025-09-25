# 🛠️ API 클라이언트 시스템 가이드

## 🎯 API 클라이언트 시스템 개요

우리 프로젝트는 **Factory 패턴과 전략 패턴**을 결합한 강력한 API 클라이언트 시스템을 제공합니다. 이를 통해 멀티 서비스 환경에서 HTTP 요청 코드를 대폭 단순화하고 유지보수성을 향상시킵니다.

## 📐 시스템 구조

```
src/test/java/com/camping/tests/support/
├── client/                              # 🎯 API 클라이언트 시스템
│   ├── ApiClient.java                   # 📋 공통 인터페이스
│   ├── BaseApiClient.java               # 🔧 공통 구현체
│   ├── ApiClientFactory.java            # 🏭 팩토리 클래스
│   └── impl/                            # 📁 서비스별 구현체
│       ├── KioskApiClient.java          # 🛒 키오스크 서비스
│       ├── AdminApiClient.java          # 👨‍💼 관리자 서비스
│       └── ReservationApiClient.java    # 📅 예약 서비스
├── helper/                              # 🔧 지원 클래스들
│   ├── ServiceType.java                 # 🏷️ 서비스 타입 정의
│   ├── ServiceContext.java              # 🌐 서비스 컨텍스트 관리
│   ├── HttpMethod.java                  # 📋 HTTP 메서드 enum
│   ├── HttpMethodStrategy.java          # ⚡ 전략 패턴 인터페이스
│   ├── GetStrategy.java                 # 🔄 GET 요청 전략
│   ├── PostStrategy.java                # 📝 POST 요청 전략
│   ├── PutStrategy.java                 # ✏️ PUT 요청 전략
│   ├── PatchStrategy.java               # 🔧 PATCH 요청 전략
│   └── DeleteStrategy.java              # 🗑️ DELETE 요청 전략
└── fixture/                             # 🧪 테스트 픽스처
    ├── KioskTestFixture.java            # 키오스크 테스트 데이터
    └── PaymentTestFixture.java          # 결제 테스트 데이터
```

---

## 🏭 ApiClientFactory - 서비스별 클라이언트 생성

### 멀티 서비스 아키텍처 지원

우리 시스템은 3개의 독립적인 마이크로서비스를 지원합니다:

| 서비스 | 포트 | 역할 | 클라이언트 |
|--------|------|------|-----------|
| **KIOSK** | 18081 | 키오스크 서비스 | `KioskApiClient` |
| **ADMIN** | 18082 | 관리자 서비스 | `AdminApiClient` |
| **RESERVATION** | 18083 | 예약 서비스 | `ReservationApiClient` |

### 기본 사용법

```java
// 1. Factory를 통한 명시적 생성
ApiClient kioskClient = ApiClientFactory.create(ServiceType.KIOSK);
ApiClient adminClient = ApiClientFactory.create(ServiceType.ADMIN);
ApiClient reservationClient = ApiClientFactory.create(ServiceType.RESERVATION);

// 2. 편의 메서드로 간편 생성
ApiClient kiosk = ApiClientFactory.kiosk();
ApiClient admin = ApiClientFactory.admin();
ApiClient reservation = ApiClientFactory.reservation();
```

### 실제 멀티 서비스 시나리오 예시

```java
// 📱 사용자가 키오스크에서 상품 조회
ExtractableResponse<Response> products = ApiClientFactory.kiosk()
    .get("/api/products");

// 📅 예약 서비스로 예약 생성
Map<String, Object> reservationData = Map.of(
    "productId", 1,
    "quantity", 2
);
ExtractableResponse<Response> reservation = ApiClientFactory.reservation()
    .post("/api/reservations", reservationData);

// 👨‍💼 관리자가 예약 승인
Map<String, String> statusUpdate = Map.of("status", "APPROVED");
long reservationId = reservation.jsonPath().getLong("id");
ExtractableResponse<Response> approval = ApiClientFactory.admin()
    .patch("/api/reservations/" + reservationId, statusUpdate, true); // 인증 필요
```

---

## 🎯 ApiClient - 공통 인터페이스

### 제공되는 메서드

모든 HTTP 메서드는 4가지 오버로드를 제공합니다:

```java
public interface ApiClient {
    // GET 메서드
    <T> ExtractableResponse<Response> get(String url);                        // 기본
    <T> ExtractableResponse<Response> get(String url, boolean needAuth);      // 인증 여부 설정
    <T> ExtractableResponse<Response> get(String url, T body);               // Body 포함
    <T> ExtractableResponse<Response> get(String url, T body, boolean auth); // 전체 옵션

    // POST, PUT, PATCH, DELETE 메서드도 동일한 패턴
}
```

### 💡 사용 팁

- **서비스 명확성**: 메서드 호출 시점에 어떤 서비스인지 명확함
- **타입 안전성**: 컴파일 타임에 잘못된 사용 방지
- **자동완성 지원**: IDE에서 완벽한 자동완성 제공
- **인증 처리**: `true` 파라미터로 JWT 토큰 자동 포함

---

## 🔧 ServiceContext - 서비스 컨텍스트 관리

### 핵심 역할

```java
public class ServiceContext {
    // 서비스별 RequestSpecification 관리
    public static void initializeRequestSpec(ServiceType serviceType);
    public static RequestSpecification getRequestSpecification(ServiceType serviceType);

    // 서비스별 인증 토큰 관리
    public static void setAccessToken(ServiceType serviceType, String token);
    public static RequestSpecification getRequestSpecificationWithAccessToken(ServiceType serviceType);

    // ThreadLocal로 테스트 격리 보장
    public static void clearContext();
}
```

### 초기화 과정 (Hooks.java)

```java
@Before
public void beforeScenario() {
    // 각 서비스별 RequestSpec 초기화
    ServiceContext.initializeRequestSpec(ServiceType.ADMIN);    // localhost:18082
    ServiceContext.initializeRequestSpec(ServiceType.KIOSK);    // localhost:18081
    ServiceContext.initializeRequestSpec(ServiceType.RESERVATION); // localhost:18083
}

@BeforeAll
public static void initAccessToken() {
    // Admin 로그인 후 토큰 획득
    String adminAccessToken = requestAdminLogin(loginParams);

    // 각 서비스에 토큰 설정 (필요한 경우)
    ServiceContext.setAccessToken(ServiceType.ADMIN, adminAccessToken);
    ServiceContext.setAccessToken(ServiceType.KIOSK, adminAccessToken);
}
```

---

## ⚡ 전략 패턴 구현

### HttpMethodStrategy 인터페이스

```java
public interface HttpMethodStrategy {
    <T> ExtractableResponse<Response> execute(RequestSpecification requestSpec, String url, T body);
    boolean supports(HttpMethod method);
}
```

### 전략 구현 예시 - PostStrategy

```java
public class PostStrategy implements HttpMethodStrategy {

    @Override
    public <T> ExtractableResponse<Response> execute(RequestSpecification requestSpec, String url, T body) {
        RequestSpecification given = RestAssured.given().spec(requestSpec);
        if (body != null) {
            given = given.body(body);
        }

        return given.when()
                .post(url)
                .then()
                .extract();
    }

    @Override
    public boolean supports(HttpMethod method) {
        return method == HttpMethod.POST;
    }
}
```

### BaseApiClient에서의 전략 활용

```java
public abstract class BaseApiClient implements ApiClient {
    private final ServiceType serviceType;
    private final List<HttpMethodStrategy> strategies = List.of(
        new GetStrategy(), new PostStrategy(), new PutStrategy(),
        new PatchStrategy(), new DeleteStrategy()
    );

    protected <T> ExtractableResponse<Response> execute(HttpMethod method, String url, T body, boolean needAuth) {
        RequestSpecification requestSpec = needAuth
            ? ServiceContext.getRequestSpecificationWithAccessToken(serviceType)
            : ServiceContext.getRequestSpecification(serviceType);

        // 적절한 전략 찾아서 실행
        for (HttpMethodStrategy strategy : strategies) {
            if (strategy.supports(method)) {
                return strategy.execute(requestSpec, url, body);
            }
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }
}
```

---

## 🧪 TestFixture에서의 활용

### KioskTestFixture 예시

```java
public class KioskTestFixture {

    public static ExtractableResponse<Response> 키오스크_상품_목록_조회() {
        ExtractableResponse<Response> response = ApiClientFactory.kiosk()
            .get("/api/products", true); // 인증 필요
        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    public static void 상품_목록_개수_검증(ExtractableResponse<Response> response, int expectedMinCount) {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(expectedMinCount);
    }
}
```

### PaymentTestFixture 예시

```java
public class PaymentTestFixture {

    public static ExtractableResponse<Response> 정상_금액으로_결제_요청(List<Map<String, Object>> selectedItems) {
        Map<String, Object> paymentRequest = createPaymentRequest(selectedItems);

        // 키오스크 서비스의 결제 API 호출
        return ApiClientFactory.kiosk()
            .post("/api/payments", paymentRequest);
    }

    public static void 결제_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }
}
```

---

## 🔍 고급 활용법

### 1. 크로스 서비스 워크플로우

```java
public class CrossServiceWorkflow {

    public static void 예약_승인_워크플로우() {
        // 1단계: Reservation 서비스에서 예약 생성
        Map<String, Object> reservationData = createReservationData();
        ExtractableResponse<Response> reservation = ApiClientFactory.reservation()
            .post("/api/reservations", reservationData);

        long reservationId = reservation.jsonPath().getLong("id");

        // 2단계: Admin 서비스에서 예약 승인
        Map<String, String> approval = Map.of("status", "APPROVED");
        ApiClientFactory.admin()
            .patch("/api/admin/reservations/" + reservationId, approval, true);

        // 3단계: Kiosk에서 승인된 예약 확인
        ExtractableResponse<Response> confirmed = ApiClientFactory.kiosk()
            .get("/api/reservations/" + reservationId);

        assertThat(confirmed.jsonPath().getString("status")).isEqualTo("APPROVED");
    }
}
```

### 2. 커스텀 인증 헤더

```java
public static ExtractableResponse<Response> 특정_토큰으로_관리자_요청(String customToken) {
    // ServiceContext에 임시 토큰 설정
    String originalToken = ServiceContext.getAccessToken(ServiceType.ADMIN);
    ServiceContext.setAccessToken(ServiceType.ADMIN, customToken);

    try {
        return ApiClientFactory.admin()
            .get("/api/admin/sensitive-data", true);
    } finally {
        // 원래 토큰으로 복원
        if (originalToken != null) {
            ServiceContext.setAccessToken(ServiceType.ADMIN, originalToken);
        }
    }
}
```

### 3. 동적 서비스 선택

```java
public static ExtractableResponse<Response> 서비스별_상태_확인(ServiceType serviceType) {
    ApiClient client = ApiClientFactory.create(serviceType);
    return client.get("/actuator/health");
}

// 사용 예시
public void 모든_서비스_상태_확인() {
    for (ServiceType serviceType : ServiceType.values()) {
        ExtractableResponse<Response> health = 서비스별_상태_확인(serviceType);
        assertThat(health.statusCode()).isEqualTo(200);
    }
}
```

---

## 🚨 주의사항 및 권장사항

### ⚠️ 하지 말아야 할 것들

```java
// ❌ 하드코딩된 포트 사용
RestAssured.get("http://localhost:18081/api/products");

// ❌ ServiceType 혼동
ApiClientFactory.admin().get("/api/kiosk-products"); // 잘못된 서비스 사용

// ❌ 직접 RestAssured 사용 (시스템 우회)
RestAssured.given()
    .baseUri("http://localhost:18082")
    .when()
    .get("/api/admin/users");
```

### ✅ 권장사항

```java
// ✅ 명확한 서비스 분리
ApiClient kioskClient = ApiClientFactory.kiosk();
ApiClient adminClient = ApiClientFactory.admin();

// ✅ 의미 있는 메서드명으로 래핑
public static ExtractableResponse<Response> 관리자_권한으로_사용자_목록_조회() {
    return ApiClientFactory.admin().get("/api/users", true);
}

// ✅ 서비스 역할에 맞는 API 호출
public static void 키오스크_상품_관리_시나리오() {
    // 키오스크에서 상품 조회
    ApiClientFactory.kiosk().get("/api/products");

    // 관리자에서 상품 관리
    ApiClientFactory.admin().post("/api/admin/products", productData, true);

    // 예약에서 상품 예약
    ApiClientFactory.reservation().post("/api/reservations", reservationData);
}
```

---

## 🎯 마이그레이션 가이드

### 기존 ApiHelper에서 새 시스템으로

#### Before (ApiHelper 사용)
```java
// 복잡하고 서비스가 불분명
createExtractableResponse("GET", "/api/products");
createExtractableResponse(ServiceType.ADMIN, "POST", "/api/users", userData, true);
```

#### After (ApiClientFactory 사용)
```java
// 명확하고 직관적
ApiClientFactory.kiosk().get("/api/products");
ApiClientFactory.admin().post("/api/users", userData, true);
```

### 장점 요약

1. **서비스 명확성** - 호출 시점에 어떤 서비스인지 바로 알 수 있음
2. **타입 안전성** - 문자열 기반 메서드명 대신 타입 안전한 메서드 호출
3. **확장성** - 새로운 서비스 추가 시 구현체만 추가하면 됨
4. **테스트 용이성** - 각 서비스별로 독립적인 테스트 가능
5. **멀티 서비스 지원** - 복잡한 크로스 서비스 시나리오 완벽 지원

---

## 🎯 정리

이 API 클라이언트 시스템을 활용하면:

1. **멀티 서비스 아키텍처 완벽 지원** - 3개 독립 서비스 간 원활한 통신
2. **코드 명확성 극대화** - 어떤 서비스를 호출하는지 한눈에 파악
3. **확장성과 유지보수성** - 새로운 서비스나 기능 추가 용이
4. **타입 안전성** - 컴파일 타임 오류 방지
5. **테스트 격리** - 각 서비스별 독립적인 테스트 환경

**Factory 패턴 + 전략 패턴**의 조합으로 현대적이고 확장 가능한 테스트 인프라를 구축했습니다! 🚀