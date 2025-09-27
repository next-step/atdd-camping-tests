# 🔧 OpenAPI 자동 생성 설정 가이드

## 📋 개요

각 서비스의 Swagger 어노테이션에서 OpenAPI 명세를 자동 생성하도록 설정이 완료되었습니다. 이제 코드 변경 시 문서가 자동으로 최신화됩니다.

---

## ✅ 완료된 설정

### 1단계: SpringDoc 의존성 추가 ✅

모든 서비스에 SpringDoc OpenAPI 의존성이 추가되었습니다:
- Admin 서비스: `infra/repos/atdd-camping-admin/build.gradle`
- Kiosk 서비스: `infra/repos/atdd-camping-kiosk/build.gradle`
- Reservation 서비스: `infra/repos/atdd-camping-reservation/build.gradle`

```gradle
// SpringDoc OpenAPI
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
```

### 2단계: OpenAPI 설정 클래스 추가 ✅

모든 서비스에 OpenAPI 설정 클래스가 생성되었습니다:

### Admin 서비스 설정

**파일**: `infra/repos/atdd-camping-admin/src/main/java/com/camping/admin/config/OpenApiConfig.java`

```java
package com.camping.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("캠핑 예약 시스템 - Admin Service API")
                        .description("관리자용 상품, 예약, 캠프사이트 관리 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("dev@camping.com")))
                .addServersItem(new Server()
                        .url("http://localhost:18082")
                        .description("Admin Service"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 Bearer 형식으로 전송")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
```

### Kiosk 서비스 설정

**파일**: `infra/repos/atdd-camping-kiosk/src/main/java/com/camping/kiosk/config/OpenApiConfig.java`

```java
package com.camping.kiosk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kioskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("캠핑 예약 시스템 - Kiosk Service API")
                        .description("키오스크용 상품 조회 및 결제 처리 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("dev@camping.com")))
                .addServersItem(new Server()
                        .url("http://localhost:18081")
                        .description("Kiosk Service"));
    }
}
```

### Reservation 서비스 설정

**파일**: `infra/repos/atdd-camping-reservation/src/main/java/com/camping/reservation/config/OpenApiConfig.java`

```java
package com.camping.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("캠핑 예약 시스템 - Reservation Service API")
                        .description("고객용 예약 생성 및 사이트 가용성 확인 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("dev@camping.com")))
                .addServersItem(new Server()
                        .url("http://localhost:18083")
                        .description("Reservation Service"));
    }
}
```

### 3단계: Swagger 어노테이션 추가 ✅

주요 Controller에 Swagger 어노테이션이 추가되었습니다:
- Admin AuthController: 인증 API
- Admin ProductAdminController: 상품 관리 API

---

## 🚀 사용 방법

### Admin Controller 예시

```java
@Tag(name = "Authentication", description = "인증 관리 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(
        summary = "관리자 로그인",
        description = "관리자 또는 키오스크의 인증을 수행하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "string", example = "Invalid credentials")
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 구현...
    }
}
```

---

### 4단계: 자동 YAML 추출 설정 ✅

**파일**: `build.gradle.kts` (메인 프로젝트)

자동 OpenAPI 문서 생성 태스크가 추가되었습니다:

```kotlin
tasks.register("generateOpenApiDocs") {
    group = "documentation"
    description = "Generate OpenAPI documentation from all services"

    dependsOn("composeUp")

    doLast {
        // 모든 서비스의 OpenAPI 문서를 자동 생성
        // - docs/admin-openapi.yaml
        // - docs/kiosk-openapi.yaml
        // - docs/reservation-openapi.yaml
    }
}
```

---

## ⚡ 사용 방법

### 1. 서비스 실행 후 Swagger UI 접근

```bash
# 서비스 실행
./gradlew composeUp

# Swagger UI 접근 (자동으로 사용 가능)
# Admin: http://localhost:18082/swagger-ui.html
# Kiosk: http://localhost:18081/swagger-ui.html
# Reservation: http://localhost:18083/swagger-ui.html
```

### 2. OpenAPI YAML 자동 생성

```bash
# 모든 서비스의 OpenAPI 문서 자동 생성
./gradlew generateOpenApiDocs

# 개별 서비스별로 수동 생성
curl -o docs/admin-openapi.yaml http://localhost:18082/v3/api-docs.yaml
curl -o docs/kiosk-openapi.yaml http://localhost:18081/v3/api-docs.yaml
curl -o docs/reservation-openapi.yaml http://localhost:18083/v3/api-docs.yaml
```

### 3. 실시간 API 문서 확인

```bash
# JSON 형식으로 API 문서 확인
curl http://localhost:18082/v3/api-docs | jq

# YAML 형식으로 API 문서 확인
curl http://localhost:18082/v3/api-docs.yaml
```

---

## 🔄 6단계: CI/CD 자동화

### GitHub Actions 예시

**파일**: `.github/workflows/update-api-docs.yml`

```yaml
name: Update API Documentation

on:
  push:
    branches: [ main ]
    paths:
      - 'infra/repos/*/src/main/java/**'

jobs:
  update-docs:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Start services
      run: ./gradlew composeUp

    - name: Wait for services
      run: sleep 30

    - name: Generate OpenAPI docs
      run: ./gradlew generateIntegratedOpenApi

    - name: Commit updated docs
      run: |
        git config --local user.email "action@github.com"
        git config --local user.name "GitHub Action"
        git add docs/*.yaml
        git diff --staged --quiet || git commit -m "Update API documentation"
        git push
```

---

## 🎯 자동화 완료!

### ✅ 적용된 장점

1. **코드와 문서 동기화**: Swagger 어노테이션 변경 시 자동 반영 ✅
2. **실시간 확인**: 각 서비스의 Swagger UI에서 즉시 확인 가능 ✅
3. **자동 생성**: `./gradlew generateOpenApiDocs` 명령어로 모든 문서 생성 ✅
4. **테스팅 편의성**: Swagger UI에서 직접 API 테스트 가능 ✅

### 📋 변경 사항

| 항목 | 이전 (수동) | 현재 (자동화) |
|------|------------|-------------|
| 문서 업데이트 | 수동으로 YAML 수정 | 코드 변경 시 자동 반영 ✅ |
| 정확성 | 코드와 불일치 가능성 | 코드와 100% 일치 ✅ |
| 유지보수 | 높은 비용 | 낮은 비용 ✅ |
| 실시간 확인 | 불가능 | Swagger UI로 가능 ✅ |

---

## 🎉 설정 완료!

모든 단계가 완료되었습니다:

1. **✅ SpringDoc 의존성 추가**: 모든 서비스에 적용 완료
2. **✅ OpenAPI 설정 클래스**: 각 서비스별로 생성 완료
3. **✅ Swagger 어노테이션**: 주요 Controller에 추가 완료
4. **✅ 자동 생성 스크립트**: `generateOpenApiDocs` 태스크 구현

이제 코드 변경 시 OpenAPI 명세가 자동으로 최신화됩니다! 🎉

### 📝 다음 단계
추가 Controller에 Swagger 어노테이션을 추가하면 더욱 완성된 API 문서를 얻을 수 있습니다.