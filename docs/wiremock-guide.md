# 🎭 WireMock JSON 기반 외부 서비스 모킹 가이드

## 📋 개요

WireMock을 사용하여 **JSON 파일 기반 stubbing**으로 외부 API를 모킹하는 방법을 다룹니다. Java 코드 없이 JSON 파일만으로 외부 서비스를 모킹할 수 있습니다.

## 🏗️ 디렉토리 구조

```
프로젝트_루트/
├── wiremock/                           # WireMock 작업 디렉토리
│   ├── mappings/                       # 📁 요청-응답 매핑 정의
│   │   ├── payment-success.json        # 💳 결제 성공 케이스
│   │   ├── payment-failure.json        # ❌ 결제 실패 케이스
│   │   ├── user-service-get.json       # 👤 사용자 조회 API
│   │   └── notification-send.json      # 📧 알림 전송 API
│   ├── __files/                        # 📁 응답 본문 파일들
│   │   ├── payment-success-response.json
│   │   ├── payment-failure-response.json
│   │   ├── user-data.json
│   │   └── notification-result.json
│   └── README.md                       # 사용법 가이드
├── wiremock-standalone.jar             # WireMock JAR 파일
└── src/test/                           # 테스트 코드
    └── ...
```

## 📝 JSON 매핑 파일 작성법

### 1. 기본 매핑 구조

**wiremock/mappings/example.json:**
```json
{
  "request": {
    "method": "POST",
    "url": "/api/external/service/endpoint",
    "headers": {
      "Content-Type": {"equalTo": "application/json"},
      "Authorization": {"matches": "Bearer .*"}
    },
    "bodyPatterns": [
      {"matchesJsonPath": "$.amount", "equalTo": "10000"}
    ]
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "bodyFileName": "success-response.json"
  }
}
```

### 2. 결제 성공 케이스 예시

**wiremock/mappings/payment-success.json:**
```json
{
  "request": {
    "method": "POST",
    "url": "/api/external/payment/confirm",
    "headers": {
      "Content-Type": {"equalTo": "application/json"},
      "Authorization": {"matches": "Bearer .*"}
    },
    "bodyPatterns": [
      {
        "matchesJsonPath": "$.amount",
        "doesNotMatch": "0"
      }
    ]
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "bodyFileName": "payment-success-response.json"
  }
}
```

**wiremock/__files/payment-success-response.json:**
```json
{
  "success": true,
  "paymentKey": "payment_{{randomValue type='ALPHANUMERIC' length=20}}",
  "orderId": "order_{{now format='yyyyMMddHHmmss'}}",
  "amount": 10000,
  "method": "CARD",
  "status": "CONFIRMED",
  "approvedAt": "{{now format='yyyy-MM-dd HH:mm:ss'}}",
  "message": "결제가 성공적으로 완료되었습니다."
}
```

### 3. 결제 실패 케이스 예시

**wiremock/mappings/payment-failure.json:**
```json
{
  "request": {
    "method": "POST",
    "url": "/api/external/payment/confirm",
    "bodyPatterns": [
      {
        "matchesJsonPath": "$.amount",
        "equalTo": "0"
      }
    ]
  },
  "response": {
    "status": 400,
    "headers": {
      "Content-Type": "application/json"
    },
    "bodyFileName": "payment-failure-response.json"
  }
}
```

**wiremock/__files/payment-failure-response.json:**
```json
{
  "success": false,
  "errorCode": "INVALID_AMOUNT",
  "message": "결제 생성 실패",
  "details": "결제 금액이 유효하지 않습니다. 0원 이상이어야 합니다."
}
```

## 🔧 고급 매핑 패턴

### 1. URL 패턴 매칭

```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/api/users/([0-9]+)",
    "queryParameters": {
      "include": {"matches": "profile|settings"}
    }
  },
  "response": {
    "status": 200,
    "headers": {"Content-Type": "application/json"},
    "bodyFileName": "user-{{request.path.[1]}}-{{request.query.include}}.json"
  }
}
```

### 2. 요청 본문 기반 분기

```json
{
  "request": {
    "method": "POST",
    "url": "/api/notifications/send",
    "bodyPatterns": [
      {"matchesJsonPath": "$.type", "equalTo": "EMAIL"},
      {"matchesJsonPath": "$.recipients[*]", "contains": "@gmail.com"}
    ]
  },
  "response": {
    "status": 200,
    "bodyFileName": "email-notification-success.json"
  }
}
```

### 3. 우선순위 설정

```json
{
  "priority": 1,
  "request": {
    "method": "POST",
    "url": "/api/special-case"
  },
  "response": {
    "status": 200,
    "body": "특별한 케이스 처리"
  }
}
```

## 🎲 동적 응답 생성

### 템플릿 변수 활용

```json
{
  "id": "{{randomValue type='UUID'}}",
  "timestamp": "{{now format='yyyy-MM-dd HH:mm:ss'}}",
  "orderId": "ORDER_{{now format='yyyyMMdd'}}_{{randomValue type='NUMERIC' length=6}}",
  "userId": "{{request.body jsonPath='$.userId'}}",
  "requestedAmount": "{{request.body jsonPath='$.amount'}}",
  "processedAt": "{{now offset='+5 seconds' format='yyyy-MM-dd HH:mm:ss'}}",
  "expiresAt": "{{now offset='+1 day' format='yyyy-MM-dd HH:mm:ss'}}"
}
```

### 사용 가능한 템플릿 함수들

| 함수 | 설명 | 예시 |
|------|------|------|
| `{{now}}` | 현재 시간 | `{{now format='yyyy-MM-dd'}}` |
| `{{randomValue}}` | 랜덤 값 생성 | `{{randomValue type='UUID'}}` |
| `{{request.path}}` | 요청 경로 | `{{request.path.[1]}}` |
| `{{request.query}}` | 쿼리 파라미터 | `{{request.query.status}}` |
| `{{request.body}}` | 요청 본문 | `{{request.body jsonPath='$.id'}}` |

## 🧪 테스트에서 활용하기

### TestFixture에서 외부 API 호출

```java
public class ExternalServiceTestFixture {

    private static final String WIREMOCK_BASE_URL = "http://localhost:8089";

    public static ExtractableResponse<Response> 외부_결제_API_호출(Map<String, Object> paymentData) {
        return RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer external-api-key")
            .body(paymentData)
            .when()
            .post(WIREMOCK_BASE_URL + "/api/external/payment/confirm")
            .then()
            .extract();
    }

    public static ExtractableResponse<Response> 외부_사용자_조회(Long userId) {
        return RestAssured.given()
            .when()
            .get(WIREMOCK_BASE_URL + "/api/external/users/" + userId)
            .then()
            .extract();
    }

    public static void 외부_API_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }

    public static void 외부_API_실패_검증(ExtractableResponse<Response> response, String expectedErrorCode) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo(expectedErrorCode);
    }
}
```

### Gherkin 시나리오에서 사용

```gherkin
Feature: 외부 서비스 연동 테스트

  Background:
    Given WireMock 서버가 실행 중이다

  Scenario: 외부 결제 API 성공 케이스
    Given 유효한 결제 정보가 준비되어 있다
    When 외부 결제 API를 호출한다
    Then 결제가 성공한다
    And 결제 키가 반환된다

  Scenario: 외부 결제 API 실패 케이스
    Given 유효하지 않은 결제 금액(0원)이 준비되어 있다
    When 외부 결제 API를 호출한다
    Then 결제가 실패한다
    And 오류 메시지가 "결제 생성 실패"이다
```

## 🚀 새로운 외부 서비스 모킹 추가하기

### 1단계: 매핑 파일 생성

**wiremock/mappings/user-service-get.json:**
```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/api/external/users/([0-9]+)"
  },
  "response": {
    "status": 200,
    "headers": {"Content-Type": "application/json"},
    "bodyFileName": "user-service-response.json"
  }
}
```

### 2단계: 응답 파일 생성

**wiremock/__files/user-service-response.json:**
```json
{
  "id": "{{request.path.[1]}}",
  "name": "테스트 사용자",
  "email": "test{{request.path.[1]}}@example.com",
  "createdAt": "{{now format='yyyy-MM-dd HH:mm:ss'}}",
  "isActive": true
}
```

### 3단계: TestFixture 메서드 추가

```java
public static ExtractableResponse<Response> 외부_사용자_조회(Long userId) {
    return RestAssured.given()
        .when()
        .get("http://localhost:8089/api/external/users/" + userId)
        .then()
        .extract();
}
```

### 4단계: 테스트에서 사용

```java
@Test
public void 외부_사용자_서비스_조회_테스트() {
    ExtractableResponse<Response> response = 외부_사용자_조회(123L);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("id")).isEqualTo("123");
    assertThat(response.jsonPath().getString("name")).isEqualTo("테스트 사용자");
}
```


## 🔍 디버깅 및 모니터링

### 1. WireMock Admin UI 활용

WireMock 서버 실행 후 Admin UI에 접속:
```
http://localhost:8089/__admin
```

기능:
- 요청/응답 로그 확인
- 매핑 상태 실시간 모니터링
- 매핑 파일 동적 추가/수정

### 2. 요청 로깅 활성화

```bash
# 상세 로깅과 함께 실행
java -jar wiremock-standalone.jar --port 8089 --root-dir wiremock --verbose
```

### 3. 매핑 파일 유효성 검사

```bash
# JSON 파일 문법 검사
find wiremock/mappings -name "*.json" -exec json_pp {} \;
```

### 4. 자주 발생하는 문제들

**문제**: 매핑 파일이 로드되지 않음
```
해결: wiremock/mappings/ 경로 확인
     JSON 파일 문법 오류 체크
     WireMock 서버 재시작
```

**문제**: 응답 파일을 찾을 수 없음
```
해결: wiremock/__files/ 디렉토리에 파일 존재 확인
     bodyFileName 경로 정확성 체크
```

**문제**: 템플릿 변수가 동작하지 않음
```
해결: WireMock 버전 확인 (3.0 이상 필요)
     템플릿 문법 정확성 체크
```

## 📚 베스트 프랙티스

### 1. 파일 네이밍 규칙

```
매핑 파일: {service}-{action}-{scenario}.json
응답 파일: {service}-{action}-{scenario}-response.json

예시:
- payment-confirm-success.json
- payment-confirm-failure.json
- user-get-active-response.json
- notification-send-email-response.json
```

### 2. 응답 지연 시뮬레이션

```json
{
  "response": {
    "status": 200,
    "fixedDelayMilliseconds": 2000,
    "bodyFileName": "slow-response.json"
  }
}
```

### 3. 조건부 장애 시뮬레이션

```json
{
  "response": {
    "status": 500,
    "fault": "CONNECTION_RESET_BY_PEER",
    "body": "서버 오류 발생"
  }
}
```

### 4. 환경별 설정 관리

```bash
# 개발 환경
java -jar wiremock-standalone.jar --port 8089 --root-dir wiremock/dev

# 테스트 환경
java -jar wiremock-standalone.jar --port 8089 --root-dir wiremock/test

# 통합 테스트 환경
java -jar wiremock-standalone.jar --port 8089 --root-dir wiremock/integration
```

## 🎯 정리

WireMock JSON 기반 모킹으로:

1. **코드 없는 모킹** - JSON 파일만으로 외부 서비스 모킹
2. **유연한 시나리오** - 성공/실패/오류 상황을 자유롭게 설정
3. **팀 협업 향상** - 비개발자도 쉽게 모킹 설정 수정 가능
4. **버전 관리** - Git으로 모킹 설정 변경 이력 추적

이를 통해 외부 의존성 없이 안정적이고 포괄적인 인수테스트를 작성할 수 있습니다! 🎉