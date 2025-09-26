# 인수테스트를 위한 서비스 API 분석 가이드

## 개요

이 문서는 인수테스트 작성 시 대상 서비스들의 API 스펙을 효과적으로 분석하고 테스트 코드에 반영하는 방법을 다룹니다. SpringBoot 기반 마이크로서비스 환경에서 Controller 소스코드를 통한 API 분석 노하우를 전수합니다.

## 1. 서비스 구조 파악

### 1.1 서비스 식별 및 역할 정의

먼저 `repos/` 디렉토리에서 각 서비스의 역할을 파악합니다:

```
repos/
├── atdd-camping-kiosk      # 키오스크 서비스 (8080)
├── atdd-camping-admin      # 관리자 서비스 (8081)
└── atdd-camping-reservation # 예약 서비스 (8082)
```

### 1.2 서비스별 포트 및 기본 설정 확인

`Service.java` 에서 각 서비스의 기본 URL을 확인합니다:

```java
public enum Service {
    KIOSK("http://localhost:8080"),
    ADMIN("http://localhost:8081"),
    RESERVATION("http://localhost:8082");

    public String getBaseUrl() {
        var env = "%s_BASE_URL".formatted(this.name());
        return System.getenv().getOrDefault(env, defaultBaseUrl);
    }
}
```

**핵심 포인트:**
- 환경변수를 통한 동적 URL 설정 가능
- 로컬 개발 환경의 기본 포트 정보 파악

## 2. SpringBoot Controller 분석 방법

### 2.1 Controller 파일 위치 찾기

각 서비스의 소스코드에서 Controller 파일을 찾는 일반적인 경로:

```
repos/{service-name}/src/main/java/{package}/controller/
repos/{service-name}/src/main/java/{package}/web/
repos/{service-name}/src/main/java/{package}/api/
```

### 2.2 API 엔드포인트 매핑 분석

Controller 클래스에서 주요 어노테이션들을 확인합니다:

```java
@RestController
@RequestMapping("/api/products")  // 기본 경로
public class ProductController {

    @GetMapping                    // GET /api/products
    public List<ProductResponse> getProducts() { ... }

    @PostMapping                   // POST /api/products
    public ResponseEntity<ProductResponse> createProduct(
        @RequestBody CreateProductRequest request) { ... }

    @PutMapping("/{id}")          // PUT /api/products/{id}
    public ProductResponse updateProduct(
        @PathVariable Long id,
        @RequestBody UpdateProductRequest request) { ... }
}
```

### 2.3 HTTP 메서드별 패턴 분석

**GET 요청 - 조회 API:**
```java
// 목록 조회
@GetMapping
public List<ProductDetail> getProducts() { ... }

// 단건 조회
@GetMapping("/{id}")
public ProductDetail getProduct(@PathVariable Long id) { ... }
```

**POST 요청 - 생성 API:**
```java
@PostMapping
public ResponseEntity<ProductDetail> createProduct(
    @RequestBody CreateProductRequest request) { ... }
```

**PUT/PATCH 요청 - 수정 API:**
```java
@PutMapping("/{id}")
public ProductDetail updateProduct(
    @PathVariable Long id,
    @RequestBody UpdateProductRequest request) { ... }
```

## 3. Request/Response DTO 구조 파악

### 3.1 Request DTO 분석

기존 테스트 코드의 Request DTO 패턴을 참고하여 실제 API 스펙을 추론합니다:

```java
@Builder
public record CreateAdminProductRequest(
    String name,           // 필수 필드
    String description,    // 선택 필드
    AdminProductType productType,  // Enum 타입
    int price,            // 숫자 타입
    int stockQuantity     // 숫자 타입
) {
    // Fixture 패턴으로 테스트 데이터 생성
    public static Fixture fixture() {
        return new Fixture();
    }
}
```

**분석 포인트:**
- Record 타입 사용으로 불변 객체 구현
- Builder 패턴으로 유연한 객체 생성
- Fixture 클래스로 테스트 데이터 관리

### 3.2 복잡한 Request 구조 분석

중첩된 객체 구조를 가진 경우:

```java
@Builder
public record KioskCreatePaymentRequest(
    List<Item> items,      // 리스트 타입의 중첩 객체
    String paymentMethod
) {
    @Builder
    public record Item(    // 내부 레코드 클래스
        long productId,
        String productName,
        int unitPrice,
        int quantity
    ) { ... }
}
```

### 3.3 Response DTO 패턴 분석

API 응답 구조 파악:

```java
// 성공/실패 정보를 포함한 응답
public record KioskCreatePaymentResult(
    boolean success,    // 성공 여부
    String message,     // 메시지
    String paymentKey,  // 결제 키
    String orderId,     // 주문 ID
    int amount         // 금액
) {}

// 단순 데이터 응답
public record KioskProductDetail(
    long id,
    String name,
    String description,
    int price,
    int stockQuantity
) {}
```

## 4. 테스트 클라이언트 구현 방법

### 4.1 기본 클라이언트 구조

각 서비스별로 전용 클라이언트 클래스를 생성합니다:

```java
public class AdminClient {
    private static RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(Service.ADMIN.getBaseUrl())  // 서비스별 Base URL
        .setContentType(ContentType.JSON)        // 기본 Content-Type
        .setAccept(ContentType.JSON)             // 기본 Accept 헤더
        .addFilters(List.of(                     // 로깅 필터 추가
            new RequestLoggingFilter(),
            new ResponseLoggingFilter()
        ))
        .build();

    public static RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
```

### 4.2 인증이 필요한 API 처리

OAuth2 토큰 기반 인증:

```java
public class AdminClient {
    public static void setAuthToken(String authToken) {
        spec = spec.auth().oauth2(authToken);  // OAuth2 토큰 설정
    }
}
```

### 4.3 API 호출 메서드 구현

Controller 분석 결과를 바탕으로 테스트 스텝 메서드를 구현합니다:

```java
public class AdminProductTestSteps {

    // POST /admin/products
    public static Response 상품을_등록한다(CreateAdminProductRequest request) {
        return AdminClient.given()
            .body(request)                    // Request Body 설정
            .when().post("/admin/products")   // HTTP 메서드 및 경로
            .thenReturn();                    // Response 반환
    }

    // PUT /admin/products/{id}
    public static Response 상품_이름을_수정한다(long productId, UpdateAdminProductRequest request) {
        return AdminClient.given()
            .body(request)
            .when().put("/admin/products/" + productId)  // Path Variable 처리
            .thenReturn();
    }

    // GET /api/products (List Response)
    public static Response 상품_목록을_조회한다() {
        return KioskClient.given()
            .get("/api/products")             // 단순 GET 요청
            .thenReturn();
    }
}
```

### 4.4 응답 검증 및 데이터 추출

```java
public class KioskProductTestSteps {

    // 상태 코드 검증
    public static void 상품_목록_조회가_성공한다(Response response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    // List 응답 처리
    public static KioskProductDetail 상품_목록에_상품이_있다(Response response, String targetName) {
        var productDetails = response.as(new TypeRef<List<KioskProductDetail>>() {});
        return productDetails.stream()
            .filter(productDetail -> productDetail.name().equals(targetName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
    }

    // 단일 객체 응답 처리
    public static KioskCreatePaymentResult 결제_생성_결과를_가져온다(Response response) {
        return response.as(KioskCreatePaymentResult.class);
    }
}
```

## 5. 실무 적용 단계별 가이드

### 5.1 새로운 API 분석 프로세스

1. **Controller 클래스 찾기**
   - `repos/{service}/src/main/java/` 하위에서 Controller 클래스 검색
   - `@RestController` 또는 `@Controller` 어노테이션이 있는 클래스 식별

2. **엔드포인트 매핑 분석**
   - `@RequestMapping`, `@GetMapping` 등의 어노테이션으로 경로 파악
   - HTTP 메서드 종류 확인 (GET, POST, PUT, DELETE 등)

3. **파라미터 구조 분석**
   - `@RequestBody`: JSON 형태의 요청 본문
   - `@PathVariable`: URL 경로 변수
   - `@RequestParam`: 쿼리 파라미터
   - `@RequestHeader`: HTTP 헤더

4. **응답 타입 분석**
   - 메서드 반환 타입 확인
   - `ResponseEntity<T>` 사용 여부
   - List 타입인지 단일 객체인지 판단

### 5.2 DTO 클래스 작성 가이드

실제 Controller에서 사용하는 DTO를 찾지 못한 경우:

```java
// 1. 간단한 구조부터 시작
public record SimpleRequest(String name, int value) {}

// 2. 실제 API 호출로 검증 후 필드 추가/수정
@Builder
public record CompleteRequest(
    String name,
    int value,
    LocalDateTime createdAt,  // 추가 필드
    List<Item> items         // 복잡한 타입
) {
    // Fixture 패턴 적용
    public static Fixture fixture() {
        return new Fixture();
    }
}
```

### 5.3 에러 처리 패턴

API 에러 응답 분석:

```java
public static void 결제_승인이_실패한다(Response response) {
    response.then().statusCode(HttpStatus.SC_OK);      // 상태 코드는 200
    var result = response.as(PaymentResult.class);
    assertThat(result.success()).isFalse();            // 내부 success 필드로 실패 판단
    assertThat(result.message()).contains("실패");      // 에러 메시지 검증
}
```

## 6. 고급 기법

### 6.1 동적 환경 설정

개발/테스트 환경별로 다른 서비스 URL 사용:

```java
// 환경변수로 Base URL 오버라이드
// export ADMIN_BASE_URL=http://test-admin.example.com
Service.ADMIN.getBaseUrl()  // 환경변수 값 또는 기본값 반환
```

### 6.2 공통 헤더 및 인터셉터 활용

```java
private static RequestSpecification createSpec() {
    return new RequestSpecBuilder()
        .setBaseUri(baseUrl)
        .addHeader("X-API-Version", "v1")      // 공통 헤더
        .addFilter(new AllureRestAssured())    // 리포트용 필터
        .build();
}
```

### 6.3 복합 시나리오 테스트

여러 API를 조합한 비즈니스 시나리오:

```java
public class PaymentScenarioSteps {
    public static void 전체_결제_프로세스를_진행한다() {
        // 1. 상품 조회
        var product = KioskProductTestSteps.상품_목록에서_상품이_조회된다("텐트");

        // 2. 결제 생성
        var request = KioskCreatePaymentRequest.fixture()
            .items(List.of(createItem(product)))
            .create();
        var response = KioskProductTestSteps.결제를_생성한다(request);
        var result = KioskProductTestSteps.결제_생성_결과를_가져온다(response);

        // 3. 결제 승인
        var confirmRequest = KioskConfirmPaymentRequest.fixture()
            .paymentKey(result.paymentKey())
            .create();
        KioskProductTestSteps.결제를_승인한다(confirmRequest);
    }
}
```

## 7. 트러블슈팅 팁

### 7.1 API 스펙 불일치 해결

- **증상**: 테스트 실행 시 400/500 에러 발생
- **해결**: Controller 소스코드와 DTO 필드명/타입 재검증
- **디버깅**: REST Assured 로그로 실제 요청/응답 확인

### 7.2 인증 관련 문제

- **증상**: 401/403 에러 발생
- **해결**: Controller의 Security 어노테이션 확인
- **방법**: `@PreAuthorize`, `@Secured` 등의 권한 설정 분석

### 7.3 JSON 직렬화/역직렬화 오류

- **증상**: Jackson 관련 예외 발생
- **해결**: DTO의 생성자, Getter/Setter 확인
- **대안**: Record 타입 사용으로 자동 처리

## 8. 체크리스트

### API 분석 완료 체크리스트
- [ ] Controller 클래스의 모든 API 엔드포인트 파악
- [ ] HTTP 메서드별 요청/응답 구조 분석
- [ ] 필수/선택 파라미터 구분
- [ ] 에러 응답 패턴 확인
- [ ] 인증/인가 요구사항 파악

### 테스트 코드 작성 체크리스트
- [ ] 서비스별 Client 클래스 구현
- [ ] Request/Response DTO 클래스 작성
- [ ] Fixture 패턴으로 테스트 데이터 관리
- [ ] API 호출 스텝 메서드 구현
- [ ] 응답 검증 로직 작성
- [ ] 에러 케이스 처리

이 가이드를 통해 SpringBoot 기반 마이크로서비스의 API를 체계적으로 분석하고, 견고한 인수테스트 코드를 작성할 수 있습니다.