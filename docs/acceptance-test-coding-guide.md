# 인수 테스트 작성 가이드

## 1. 프로젝트 소개


### 시스템 구성 요소
- **Admin Service**: 관리자용 상품/예약 관리 시스템
- **Kiosk Service**: 고객용 키오스크 주문 시스템
- **Reservation Service**: 예약 관리 시스템
- **Payment Service**: 외부 결제 서비스 (WireMock으로 모킹)

## 2. 개발 환경 설정

### 필수 요구사항
```bash
# 프로젝트 클론 후 인프라 설정
./gradlew setupTestInfra
```

## 3. 테스트 아키텍처 설계 원칙

### 계층화된 구조
```
src/test/java/com/camping/tests/
├── scenario/           # 고수준 시나리오 스텝
│   ├── auth/          # 인증 관련 시나리오
│   ├── payment/       # 결제 관련 시나리오
│   └── product/       # 상품 관련 시나리오
├── steps/             # 서비스별 세부 스텝
│   ├── admin/         # 관리자 서비스 스텝
│   ├── kiosk/         # 키오스크 서비스 스텝
│   └── reservation/   # 예약 서비스 스텝
├── hooks/             # 테스트 생명주기 관리
└── utils/             # 테스트 유틸리티
```

### 테스트 러너 설정
```java
@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.camping.tests")
public class RunCucumberTest {
}
```

### 서비스별 클라이언트 패턴
```java
public class KioskClient {
    private static final RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(Service.KIOSK.getBaseUrl())
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addFilters(List.of(
            new RequestLoggingFilter(),
            new ResponseLoggingFilter()
        ))
        .build();

    public static RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
```

### 환경별 서비스 URL 관리
```java
@RequiredArgsConstructor
public enum Service {
    KIOSK("http://localhost:8080"),
    ADMIN("http://localhost:8081"),
    RESERVATION("http://localhost:8082");

    private final String defaultBaseUrl;

    public String getBaseUrl() {
        var env = "%s_BASE_URL".formatted(this.name());
        return System.getenv().getOrDefault(env, defaultBaseUrl);
    }
}
```

## 4. 한국어 Gherkin 시나리오 작성법

### 기본 문법과 키워드
```gherkin
Feature: 상품 등록 시나리오

  Background:
    Given 관리자 계정으로 로그인이 되어있다

  Scenario: 상품을 등록한다
    When 어드민에서 '텐트' 상품을 등록한다
    Then 어드민에서 상품 등록이 성공한다
    And 키오스크에서 '텐트' 상품이 조회된다
```

### 시나리오 명명 규칙
- **Feature**: `[도메인] + 시나리오` (예: `상품 등록 시나리오`)
- **Scenario**: 구체적인 행동과 결과 (예: `상품을 등록한다`)
- **Background**: 공통 전제 조건 정의

### 정상/예외 케이스 설계
```gherkin
Scenario: 상품 결제에 실패한다
  Given 키오스크에서 '텐트' 상품의 결제를 생성한다
  And 키오스크에서 결제 생성이 성공한다
  When 키오스크에서 잘못된 결제 키로 결제를 승인한다
  Then 키오스크에서 결제 승인이 실패한다
```

## 5. Step Definitions 구현 가이드

### 한국어 메소드명과 애노테이션 매핑
```java
public class AuthScenarioSteps {
    @Given("관리자 계정으로 로그인이 되어있다")
    public void 관리자_계정으로_로그인이_되어있다() {
        어드민으로_로그인이_되어있다();
    }
}
```

### 서비스별 스텝 정의 구조
```java
public class KioskProductTestSteps {
    // 상품 목록 조회
    public static KioskProductDetail 상품_목록에서_상품이_조회된다(String targetName) {
        var 상품_목록_조회_응답 = 상품_목록을_조회한다();
        상품_목록_조회가_성공한다(상품_목록_조회_응답);
        return 상품_목록에_상품이_있다(상품_목록_조회_응답, targetName);
    }

    public static Response 상품_목록을_조회한다() {
        return KioskClient.given()
            .get("/api/products")
            .thenReturn();
    }

    public static void 상품_목록_조회가_성공한다(Response response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    }
}
```

### 시나리오 레벨 vs 서비스 레벨 구분
- **시나리오 레벨**: 비즈니스 워크플로우 조합
- **서비스 레벨**: 개별 API 호출과 검증

## 6. API 테스트 클라이언트 구현

### 인증 토큰 관리
```java
public class AdminClient {
    private static RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(Service.ADMIN.getBaseUrl())
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addFilters(List.of(
            new RequestLoggingFilter(),
            new ResponseLoggingFilter()
        ))
        .build();

    public static void setAuthToken(String authToken) {
        spec = spec.auth().oauth2(authToken);
    }

    public static RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
```

### API 호출 패턴
```java
public static Response 상품을_등록한다(CreateAdminProductRequest request) {
    return AdminClient.given()
        .body(request)
        .post("/api/admin/products")
        .thenReturn();
}
```

## 7. 테스트 데이터 관리

### Record 기반 DTO 설계
```java
public record KioskProductDetail(
    long id,
    String name,
    int price,
    int stockQuantity,
    String productType
) {}
```

### TestContext를 통한 상태 관리
```java
public class TestContext {
    public static void clear() {
        Product.context.clear();
        Payment.context.clear();
    }

    public static class Product {
        private static Map<Key, Object> context = new EnumMap<>(Key.class);

        private enum Key {
            상품_등록_응답,
            상품_수정_응답,
        }

        public static Response 상품_등록_응답() {
            return (Response) context.get(Key.상품_등록_응답);
        }

        public static void 상품_등록_응답(Response 상품_등록_응답) {
            context.put(Key.상품_등록_응답, 상품_등록_응답);
        }
    }
}
```

### 데이터베이스 관리 유틸리티
```java
public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/atdd";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";

    public static void truncateAllTables() throws SQLException {
        List<String> tableNames = getAllTableNames();

        try (var statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (var tableName : tableNames) {
                String truncateQuery = "TRUNCATE TABLE " + tableName;
                statement.execute(truncateQuery);
            }

            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
```

## 8. 인프라 및 외부 서비스 모킹

### Docker Compose 인프라 설정
```yaml
# docker-compose-infra.yml
services:
  db:
    image: mysql:8.0
    container_name: atdd-db
    environment:
      - MYSQL_ROOT_PASSWORD=secret
      - MYSQL_DATABASE=atdd
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-psecret"]
      interval: 5s
      timeout: 3s
      retries: 20

  payments-mock:
    container_name: payments-mock
    image: wiremock/wiremock:3.13.1
    ports:
      - "8090:8080"
    volumes:
      - ./wiremock/mappings:/home/wiremock/mappings
```

### WireMock 매핑 설정
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/v1/payments/confirm"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "paymentKey": "{{jsonPath request.body '$.paymentKey'}}",
      "orderId": "{{jsonPath request.body '$.orderId'}}",
      "method": "CARD",
      "approvedAt": "{{now format='yyyy-MM-dd HH:mm:ss'}}",
      "status": "CONFIRMED"
    }
  }
}
```

## 9. 베스트 프랙티스 및 주의사항

### 테스트 코드 품질 관리
1. **단일 책임 원칙**: 하나의 스텝은 하나의 책임만
2. **재사용성**: 공통 스텝은 static 메소드로 추출
3. **가독성**: 한국어 메소드명으로 의도 명확화

### 시나리오 유지보수성 확보
1. **Background 활용**: 공통 전제 조건 중복 제거
2. **시나리오 독립성**: 각 시나리오는 서로 독립적으로 실행 가능
3. **명확한 검증**: 예상 결과를 구체적으로 명시

### 성능 최적화 팁
1. **데이터베이스 트랜잭션**: 테스트 간 격리 보장
2. **컨테이너 재사용**: 인프라 시작/중지 최소화
3. **병렬 실행**: 독립적인 테스트는 병렬 실행

### 일반적인 안티패턴 및 해결방법

#### ❌ 잘못된 패턴
```java
// 테스트에서 직접 비즈니스 로직 구현
@When("상품을 등록한다")
public void 상품을_등록한다() {
    // 복잡한 비즈니스 로직이 테스트에 노출됨
    if (product.getPrice() > 0 && product.getStock() > 0) {
        // ...
    }
}
```

#### ✅ 올바른 패턴
```java
// 명확하고 단순한 API 호출
@When("어드민에서 {string} 상품을 등록한다")
public void 어드민에서_상품을_등록한다(String 상품명) {
    var request = new CreateAdminProductRequest(상품명, 1000, 10, "RENTAL");
    var response = AdminProductTestSteps.상품을_등록한다(request);
    TestContext.Product.상품_등록_응답(response);
}
```

---

## 결론

이 가이드를 통해 한국어 BDD 기반의 체계적인 ATDD 테스트를 작성할 수 있습니다. 비즈니스 요구사항을 정확히 반영하고 유지보수하기 쉬운 테스트 코드를 작성하여, 신뢰할 수 있는 캠핑 예약 시스템을 구축해보세요.
