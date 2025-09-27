# 🔐 캠핑 예약 시스템 인증 가이드

## 📋 개요

캠핑 예약 시스템의 각 서비스별 인증 방식과 토큰 사용법을 정리한 가이드입니다. 실제 코드 분석을 통해 언제 어떤 토큰이 필요한지 명확히 설명합니다.

## 🎯 토큰 종류 및 획득 방법

### JWT Bearer Token
- **발급처**: Admin 서비스 (`/auth/login`)
- **사용처**: Admin API, Kiosk의 Admin 연동 API
- **형식**: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### Admin 로그인으로 토큰 획득

```java
// 1. Admin 서비스 로그인
Map<String, String> loginRequest = Map.of(
    "username", "admin",
    "password", "admin123"
);

ExtractableResponse<Response> response = RestAssured.given()
    .header("Content-Type", "application/json")
    .body(loginRequest)
    .when()
    .post("http://localhost:18082/auth/login")
    .then()
    .statusCode(200)
    .extract();

// 2. JWT 토큰 추출
String accessToken = response.jsonPath().get("accessToken");

// 3. 이후 API 호출 시 사용
ApiClientFactory.admin()
    .get("/admin/products", true); // needAuth=true로 자동 토큰 포함
```

## 🏗️ 서비스별 인증 정책

### 🛠️ ADMIN 서비스 (포트: 18082)

**인증 방식**: JWT Bearer Token **필수**

#### ✅ 인증 필요 API (모든 `/admin/*` 경로)

```java
// 상품 관리 API
ApiClientFactory.admin().get("/admin/products", true);              // 상품 목록 조회
ApiClientFactory.admin().post("/admin/products", productData, true); // 상품 생성
ApiClientFactory.admin().put("/admin/products/1", productData, true); // 상품 수정

// 예약 관리 API
ApiClientFactory.admin().get("/admin/reservations", true);          // 예약 목록 조회
ApiClientFactory.admin().patch("/admin/reservations/1/status", statusData, true); // 예약 상태 변경

// 기타 관리 API
ApiClientFactory.admin().get("/admin/campsites", true);             // 캠프사이트 관리
ApiClientFactory.admin().get("/admin/revenue", true);              // 매출 관리
```

#### 🔓 인증 불필요 API

```java
// 로그인 API만 인증 불필요
RestAssured.given()
    .header("Content-Type", "application/json")
    .body(loginRequest)
    .post("http://localhost:18082/auth/login");  // 인증 불필요
```

### 🛒 KIOSK 서비스 (포트: 18081)

**인증 방식**: 상품 조회는 Admin 토큰 필요, 결제는 인증 불필요

#### ✅ 인증 필요 API (Admin 연동)

```java
// 상품 목록 조회 (Admin 서비스 연동)
ApiClientFactory.kiosk().get("/api/products", true);  // Admin 토큰 필요

// 실제 내부 구현: AdminAuthClient가 Admin 로그인 후 토큰 사용
// Kiosk는 Admin에서 상품 정보를 가져오기 때문에 Admin 토큰 필요
```

#### 🔓 인증 불필요 API

```java
// 결제 API들
ApiClientFactory.kiosk().post("/api/payments", paymentData);        // 결제 생성
ApiClientFactory.kiosk().post("/api/payments/confirm", confirmData); // 결제 확인

// 기타 공개 API
ApiClientFactory.kiosk().get("/");  // 홈페이지
```

### 📅 RESERVATION 서비스 (포트: 18083)

**인증 방식**: 대부분 인증 불필요 (고객용 API)

#### 🔓 인증 불필요 API (고객용)

```java
// 예약 관련 API (고객용)
ApiClientFactory.reservation().post("/api/reservations", reservationData);  // 예약 생성
ApiClientFactory.reservation().get("/api/reservations/1");                  // 예약 조회
ApiClientFactory.reservation().put("/api/reservations/1", updateData);      // 예약 수정
ApiClientFactory.reservation().delete("/api/reservations/1?confirmationCode=ABC"); // 예약 취소

// 내 예약 조회 (이름, 전화번호로 확인)
ApiClientFactory.reservation().get("/api/reservations/my?name=홍길동&phone=010-1234-5678");

// 캘린더 조회
ApiClientFactory.reservation().get("/api/reservations/calendar?year=2024&month=12&siteId=1");
```

## 🔧 인증 구현 방식

### ServiceContext를 통한 토큰 관리

```java
// 1. 테스트 시작 시 토큰 설정 (@BeforeAll)
@BeforeAll
public static void initAccessToken() {
    // Admin 로그인
    Map<String, String> params = Map.of("username", "admin", "password", "admin123");
    String adminAccessToken = requestAdminLogin(params);

    // 각 서비스에 토큰 설정
    ServiceContext.setAccessToken(ServiceType.ADMIN, adminAccessToken);
    ServiceContext.setAccessToken(ServiceType.KIOSK, adminAccessToken);  // Kiosk도 Admin 토큰 사용
    // RESERVATION은 토큰이 필요 없으므로 설정하지 않음
}

// 2. API 호출 시 자동 토큰 포함
public static ExtractableResponse<Response> 관리자_상품_목록_조회() {
    return ApiClientFactory.admin()
        .get("/admin/products", true);  // needAuth=true 시 자동으로 Bearer 토큰 포함
}
```

### 토큰이 자동 포함되는 과정

```java
// BaseApiClient.execute() 메서드에서 처리
protected <T> ExtractableResponse<Response> execute(HttpMethod method, String url, T body, boolean needAuth) {
    RequestSpecification requestSpec = needAuth
        ? ServiceContext.getRequestSpecificationWithAccessToken(serviceType)  // 토큰 포함
        : ServiceContext.getRequestSpecification(serviceType);                // 토큰 없음

    // 실제 HTTP 요청 실행
}

// ServiceContext에서 토큰 포함 RequestSpec 생성
public static RequestSpecification getRequestSpecificationWithAccessToken(ServiceType serviceType) {
    String accessToken = getAccessToken(serviceType);
    if (accessToken == null) {
        accessToken = "dummy-token";  // 폴백 토큰
    }
    return getRequestSpecification(serviceType)
        .header("Authorization", "Bearer " + accessToken);  // Bearer 토큰 자동 포함
}
```

## 📝 실제 사용 예시

### TestFixture에서 인증 처리

```java
public class AdminTestFixture {

    // ✅ 인증 필요한 API - needAuth=true
    public static ExtractableResponse<Response> 관리자_상품_목록_조회() {
        return ApiClientFactory.admin()
            .get("/admin/products", true);  // Admin 토큰 필요
    }

    public static ExtractableResponse<Response> 관리자_상품_생성(Map<String, Object> productData) {
        return ApiClientFactory.admin()
            .post("/admin/products", productData, true);  // Admin 토큰 필요
    }

    public static ExtractableResponse<Response> 관리자_예약_상태_변경(Long reservationId, String status) {
        Map<String, String> statusData = Map.of("status", status);
        return ApiClientFactory.admin()
            .patch("/admin/reservations/" + reservationId + "/status", statusData, true);  // Admin 토큰 필요
    }
}

public class KioskTestFixture {

    // ✅ 인증 필요한 API - Admin 연동
    public static ExtractableResponse<Response> 키오스크_상품_목록_조회() {
        return ApiClientFactory.kiosk()
            .get("/api/products", true);  // Admin 토큰 필요 (내부적으로 Admin API 호출)
    }

    // 🔓 인증 불필요한 API
    public static ExtractableResponse<Response> 키오스크_결제_생성(Map<String, Object> paymentData) {
        return ApiClientFactory.kiosk()
            .post("/api/payments", paymentData);  // 인증 불필요
    }
}

public class ReservationTestFixture {

    // 🔓 모든 API 인증 불필요 (고객용)
    public static ExtractableResponse<Response> 예약_생성(Map<String, Object> reservationData) {
        return ApiClientFactory.reservation()
            .post("/api/reservations", reservationData);  // 인증 불필요
    }

    public static ExtractableResponse<Response> 예약_조회(Long reservationId) {
        return ApiClientFactory.reservation()
            .get("/api/reservations/" + reservationId);  // 인증 불필요
    }

    public static ExtractableResponse<Response> 내_예약_조회(String name, String phone) {
        return ApiClientFactory.reservation()
            .get("/api/reservations/my?name=" + name + "&phone=" + phone);  // 인증 불필요
    }
}
```

### Gherkin 시나리오에서 사용

```gherkin
Feature: 인증이 필요한 관리자 기능

  Scenario: 관리자가 상품을 관리한다
    When 관리자가 상품 목록을 조회한다    # needAuth=true, Admin 토큰 사용
    Then 상품 목록이 조회된다
    When 관리자가 새로운 상품을 등록한다   # needAuth=true, Admin 토큰 사용
    Then 상품이 등록된다

Feature: 인증이 불필요한 고객 기능

  Scenario: 고객이 예약을 한다
    When 고객이 예약을 생성한다          # 인증 불필요
    Then 예약이 생성된다
    When 고객이 예약을 조회한다          # 인증 불필요
    Then 예약 정보가 조회된다
```

## 🚨 주의사항

### 1. 토큰 필요 여부 판단

```java
// ✅ 올바른 사용
ApiClientFactory.admin().get("/admin/products", true);     // Admin API는 항상 인증 필요
ApiClientFactory.kiosk().get("/api/products", true);       // Kiosk 상품조회는 Admin 연동으로 인증 필요
ApiClientFactory.reservation().post("/api/reservations", data); // Reservation은 고객용이라 인증 불필요

// ❌ 잘못된 사용
ApiClientFactory.admin().get("/admin/products", false);    // Admin API에 인증 없이 호출 - 401 에러
ApiClientFactory.kiosk().get("/api/products", false);      // Kiosk 상품조회에 인증 없이 호출 - 실패
```

### 2. 토큰 설정 확인

```java
// 테스트 실패 시 확인사항
@Test
public void 인증_테스트() {
    // 1. ServiceContext에 토큰이 설정되어 있는지 확인
    String adminToken = ServiceContext.getAccessToken(ServiceType.ADMIN);
    assertThat(adminToken).isNotNull();

    // 2. 올바른 needAuth 설정인지 확인
    ExtractableResponse<Response> response = ApiClientFactory.admin()
        .get("/admin/products", true);  // 반드시 true

    assertThat(response.statusCode()).isEqualTo(200);
}
```

### 3. 401 Unauthorized 오류 해결

```java
// 문제: 401 Unauthorized 에러 발생
// 해결방법:

// 1. @BeforeAll에서 토큰 획득이 제대로 되었는지 확인
@BeforeAll
public static void setup() {
    // Admin 로그인 및 토큰 설정이 정상적으로 실행되는지 확인
    log.info("Admin token: {}", ServiceContext.getAccessToken(ServiceType.ADMIN));
}

// 2. API 호출 시 needAuth=true 설정 확인
public static void 올바른_인증_API_호출() {
    // Admin API는 항상 needAuth=true 필요
    ApiClientFactory.admin().get("/admin/products", true);

    // Kiosk 상품조회도 needAuth=true 필요 (Admin 연동)
    ApiClientFactory.kiosk().get("/api/products", true);
}
```

## 📊 인증 정책 요약표

| 서비스 | 엔드포인트 | 인증 필요 | 토큰 종류 | needAuth | 비고 |
|--------|------------|-----------|-----------|----------|------|
| **Admin** | `/auth/login` | ❌ | - | `false` | 로그인 API |
| **Admin** | `/admin/*` | ✅ | JWT Bearer | `true` | 모든 관리 API |
| **Kiosk** | `/api/products` | ✅ | JWT Bearer | `true` | Admin 연동 필요 |
| **Kiosk** | `/api/payments` | ❌ | - | `false` | 결제 API |
| **Kiosk** | `/api/payments/confirm` | ❌ | - | `false` | 결제 확인 API |
| **Reservation** | `/api/reservations` | ❌ | - | `false` | 고객용 예약 API |
| **Reservation** | `/api/reservations/*` | ❌ | - | `false` | 모든 고객용 API |

## 🎯 정리

1. **Admin 서비스**: 모든 API에서 JWT Bearer Token 필수
2. **Kiosk 서비스**: 상품 조회는 Admin 토큰 필요, 결제는 불필요
3. **Reservation 서비스**: 모든 API에서 인증 불필요 (고객용)
4. **토큰 관리**: ServiceContext를 통해 자동 관리
5. **사용법**: `needAuth=true`로 자동 토큰 포함

이 가이드를 따라 각 서비스의 특성에 맞는 인증 처리로 안정적인 인수테스트를 작성하세요! 🎉