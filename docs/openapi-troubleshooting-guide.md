# 🔧 OpenAPI 자동 생성 문제 해결 및 구조 가이드

## 📋 문제 해결 과정

### 🚨 발생했던 문제들

#### 1. Admin Service OpenAPI 접근 불가
```
{"error":"Missing or invalid token"}
```

#### 2. Kiosk/Reservation Service 404 오류
```
{"timestamp":"2025-09-27T15:30:04.914+00:00","status":404,"error":"Not Found","path":"/v3/api-docs.yaml"}
```

---

## 🔍 문제 원인 분석

### Admin Service 문제
**원인**: JWT 인증 필터가 OpenAPI 엔드포인트까지 차단
- JWT 필터가 모든 경로 (`/*`)에 적용
- `/v3/api-docs` 및 `/v3/api-docs.yaml` 경로가 인증 대상에 포함됨
- SpringDoc OpenAPI 엔드포인트가 JWT 토큰 없이 접근 불가

### Kiosk/Reservation Service 문제
**원인**: SpringDoc 의존성이 Docker 이미지에 반영되지 않음
- 로컬에서 `build.gradle`에 SpringDoc 의존성 추가
- 하지만 기존 Docker 컨테이너는 이전 버전 사용
- 새로운 의존성이 포함된 이미지 재빌드 필요

---

## 🛠️ 해결 과정

### 1단계: SpringDoc 의존성 추가 ✅
```gradle
// 모든 서비스 build.gradle에 추가
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
```

### 2단계: OpenAPI 설정 클래스 생성 ✅
각 서비스별로 OpenAPI 메타데이터 설정:
- Admin: JWT 보안 스키마 포함
- Kiosk: 기본 설정
- Reservation: 기본 설정

### 3단계: Admin JWT 필터 수정 ✅
**문제**: OpenAPI 엔드포인트가 JWT 필터에 막힘
**해결**: `JwtAuthFilter.java`의 `isExcluded()` 메서드에 경로 추가

```java
// Before
private boolean isExcluded(String path) {
    return pathMatcher.match("/auth/login", path) ||
           pathMatcher.match("/login", path) ||
           // ... 기타 경로들
           path.equals("/");
}

// After
private boolean isExcluded(String path) {
    return pathMatcher.match("/auth/login", path) ||
           pathMatcher.match("/login", path) ||
           // ... 기타 경로들
           pathMatcher.match("/v3/api-docs/**", path) ||
           pathMatcher.match("/v3/api-docs.yaml", path) ||
           pathMatcher.match("/swagger-ui/**", path) ||
           pathMatcher.match("/swagger-ui.html", path) ||
           path.equals("/");
}
```

### 4단계: Docker 이미지 재빌드 ✅
**문제**: 변경사항이 컨테이너에 반영되지 않음
**해결**:
1. 기존 컨테이너 및 이미지 제거
2. `./gradlew composeUp`으로 새로운 의존성 포함하여 재빌드

### 5단계: 자동 생성 태스크 구현 ✅
`build.gradle.kts`에 Gradle 태스크 추가

---

## 🏗️ 자동 생성 구조

### SpringDoc이 OpenAPI를 생성하는 과정

```mermaid
graph TD
    A[SpringDoc 의존성] --> B[OpenApiConfig 클래스]
    B --> C[Controller Swagger 어노테이션]
    C --> D[런타임 시 자동 스캔]
    D --> E[OpenAPI 3.0 명세 생성]
    E --> F[/v3/api-docs 엔드포인트]
    E --> G[/v3/api-docs.yaml 엔드포인트]
    F --> H[Swagger UI]
    G --> I[YAML 파일 다운로드]
```

### 1. SpringDoc 자동 설정
```java
// 의존성 추가 시 자동으로 활성화
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'

// SpringDoc이 자동으로 제공하는 엔드포인트
// http://localhost:8080/v3/api-docs        (JSON)
// http://localhost:8080/v3/api-docs.yaml   (YAML)
// http://localhost:8080/swagger-ui.html    (UI)
```

### 2. 어노테이션 기반 문서 생성
```java
@Tag(name = "Product Management", description = "상품 관리 API")
@RestController
@SecurityRequirement(name = "BearerAuth")
public class ProductAdminController {

    @Operation(summary = "상품 목록 조회", description = "모든 상품의 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "실패")
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        // 구현...
    }
}
```

### 3. 런타임 스캔 및 문서 생성
1. **애플리케이션 시작 시**: SpringDoc이 모든 Controller 스캔
2. **어노테이션 분석**: `@Operation`, `@ApiResponse`, `@Tag` 등 수집
3. **OpenAPI 명세 생성**: 수집된 정보를 OpenAPI 3.0 형식으로 변환
4. **엔드포인트 제공**: `/v3/api-docs`로 실시간 접근 가능

### 4. Gradle 태스크 통합
```kotlin
tasks.register("generateOpenApiDocs") {
    group = "documentation"
    description = "Generate OpenAPI documentation from all services"

    dependsOn("composeUp")  // 서비스 먼저 시작

    doLast {
        // 각 서비스의 /v3/api-docs.yaml 엔드포인트에서 YAML 다운로드
        project.exec {
            commandLine("curl", "-o", "docs/admin-openapi.yaml",
                       "http://localhost:18082/v3/api-docs.yaml")
        }
        // ... 다른 서비스들도 동일
    }
}
```

---

## 🎯 왜 자동 생성이 가능한가?

### 1. 어노테이션 기반 메타데이터
- **코드와 문서 일체화**: 어노테이션으로 API 정보를 코드에 직접 기술
- **컴파일 타임 검증**: 타입 안전성 보장
- **런타임 수집**: 실행 시점에 모든 정보를 수집하여 문서 생성

### 2. SpringDoc의 자동 설정
```java
// SpringDoc이 자동으로 수행하는 작업들
@AutoConfiguration
public class SpringDocConfiguration {

    // 1. Controller 스캔 설정
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/**")
            .build();
    }

    // 2. 엔드포인트 자동 등록
    // /v3/api-docs
    // /v3/api-docs.yaml
    // /swagger-ui.html

    // 3. 보안 스키마 통합
    // JWT, OAuth2 등 자동 감지
}
```

### 3. 실시간 동기화
- **코드 변경 → 재시작 → 문서 자동 업데이트**
- **수동 YAML 편집 불필요**
- **개발자가 어노테이션만 수정하면 문서도 함께 변경**

---

## 🚀 최종 결과

### ✅ 성공한 구조
```bash
# 서비스 시작
./gradlew composeUp

# OpenAPI 문서 자동 생성
./gradlew generateOpenApiDocs

# 결과 파일들
docs/admin-openapi.yaml      # 596줄, 완전한 Admin API 명세
docs/kiosk-openapi.yaml      # 158줄, Kiosk API 명세
docs/reservation-openapi.yaml # 421줄, Reservation API 명세
```

### 🎉 핵심 성과
1. **코드 변경 시 문서 자동 동기화**
2. **개발자 친화적인 Swagger UI 제공**
3. **CI/CD 파이프라인 통합 가능**
4. **API 테스트 직접 실행 가능**

이제 Controller에 어노테이션만 추가/수정하면 OpenAPI 문서가 자동으로 최신화됩니다! 🎯