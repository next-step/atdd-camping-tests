# ATDD Camping Tests

이 프로젝트는 캠핑 예약 시스템의 **통합 테스트(Integration Test) 및 ATDD(Acceptance Test-Driven Development)** 수행을 위한 레포지토리입니다.

## 프로젝트 구조

```
atdd-camping-tests/
├── buildSrc/                          # Gradle Convention Plugin
│   ├── build.gradle.kts               # buildSrc 자체 빌드 설정
│   └── src/main/kotlin/
│       └── docker-compose-convention.gradle.kts  # Docker 태스크 정의
├── infra/                             # 인프라 설정
│   ├── docker-compose.yml             # 애플리케이션 서비스
│   ├── docker-compose-infra.yml       # 인프라 서비스 (DB 등)
│   ├── dockerfiles/                   # 각 서비스별 Dockerfile
│   │   ├── Dockerfile-kiosk
│   │   ├── Dockerfile-admin
│   │   └── Dockerfile-reservation
│   └── repos/                         # 자동 클론되는 소스 저장소
│       ├── atdd-camping-kiosk/
│       ├── atdd-camping-admin/
│       └── atdd-camping-reservation/
├── src/test/                          # ATDD 테스트 코드
│   ├── java/
│   └── resources/
│       └── features/                  # Cucumber 기능 정의
├── build.gradle.kts                   # 메인 빌드 스크립트
└── CLAUDE.md                          # 프로젝트 문서
```

## 주요 특징

### 1. 통합 테스트 환경
- **목적**: 여러 마이크로서비스간의 통합 테스트 수행
- **대상 서비스**:
  - Kiosk 서비스 (포트: 18081)
  - Admin 서비스 (포트: 18082)
  - Reservation 서비스 (포트: 18083)

### 2. 자동화된 소스 관리
- `composeUp` 실행 시 자동으로 필요한 소스 저장소를 클론
- 각 서비스별 독립적인 빌드 환경 구성

## Gradle 태스크 구조

### buildSrc Convention Plugin
- **위치**: `buildSrc/src/main/kotlin/docker-compose-convention.gradle.kts`
- **적용**: `build.gradle.kts`에서 `id("docker-compose-convention")` 플러그인으로 적용

### 주요 태스크

#### `composeUp`
```kotlin
tasks.register<Exec>("composeUp") {
    group = "docker"
    description = "Start all services"
    workingDir = file("infra")

    doFirst {
        // 자동 소스 클론 로직
        val kioskRepoDir = file("infra/repos/atdd-camping-kiosk")
        if (!kioskRepoDir.exists()) {
            file("infra/repos").mkdirs()
            project.exec {
                commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1",
                          "git@github.com:next-step/atdd-camping-kiosk.git", "infra/repos/atdd-camping-kiosk")
            }
        }
        // ... admin, reservation 서비스도 동일하게 클론
    }

    commandLine("docker", "compose", "up", "-d")
}
```

#### `composeDown`
```kotlin
tasks.register<Exec>("composeDown") {
    group = "docker"
    description = "Stop all services"
    workingDir = file("infra")
    commandLine("docker", "compose", "down")
}
```

#### `smokeTest`
```kotlin
tasks.register("smokeTest") {
    group = "verification"
    description = "Run smoke tests"
    dependsOn("composeUp", "test")
    finalizedBy("composeDown")
}
```

## Docker 구성

### 인프라 서비스 (docker-compose-infra.yml)
```yaml
services:
  db:
    image: mysql:8.0
    container_name: atdd-db
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=secret
      - MYSQL_DATABASE=atdd
```

### 애플리케이션 서비스 (docker-compose.yml)
```yaml
services:
  kiosk:
    build:
      context: ..
      dockerfile: infra/dockerfiles/Dockerfile-kiosk
    container_name: atdd-kiosk
    ports:
      - "18081:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:kioskdb

  admin:
    build:
      context: ..
      dockerfile: infra/dockerfiles/Dockerfile-admin
    container_name: atdd-admin
    ports:
      - "18082:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:admindb

  reservation:
    build:
      context: ..
      dockerfile: infra/dockerfiles/Dockerfile-reservation
    container_name: atdd-reservation
    ports:
      - "18083:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:reservationdb
```

### Dockerfile 구성
각 서비스별로 독립적인 Dockerfile 구성:

```dockerfile
# Dockerfile-kiosk 예시
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# 해당 서비스의 소스를 빌드
COPY infra/repos/atdd-camping-kiosk/gradlew infra/repos/atdd-camping-kiosk/gradlew.bat ./
COPY infra/repos/atdd-camping-kiosk/gradle ./gradle/
COPY infra/repos/atdd-camping-kiosk/build.gradle* infra/repos/atdd-camping-kiosk/settings.gradle* ./
COPY infra/repos/atdd-camping-kiosk/src ./src/

RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:17-jdk
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## 사용법

### 1. 인프라 시작
```bash
cd infra
docker compose -f docker-compose-infra.yml up -d
```

### 2. 애플리케이션 서비스 시작
```bash
./gradlew composeUp
```

### 3. 통합 테스트 실행
```bash
./gradlew smokeTest
```

### 4. 서비스 종료
```bash
./gradlew composeDown
```

## 테스트 기술 스택

- **Cucumber**: BDD 기반 기능 테스트
- **REST Assured**: API 테스트
- **JUnit Platform**: 테스트 실행 플랫폼
- **H2 Database**: 각 서비스별 독립적인 인메모리 DB

## 네트워크 구성

- **네트워크**: `atdd-net` (external)
- 모든 서비스가 동일한 Docker 네트워크에서 통신
- 각 서비스는 고유한 포트로 외부 접근 가능

## 개발 가이드라인

1. **buildSrc 사용**: Gradle 태스크 정의는 buildSrc에서 관리
2. **자동 클론**: 소스 저장소는 태스크 실행 시 자동으로 관리됨
3. **독립적 DB**: 각 서비스는 독립적인 H2 인메모리 DB 사용
4. **Convention Plugin**: 재사용 가능한 빌드 로직을 플러그인으로 분리