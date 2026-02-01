# Docker / 인프라

## Docker 네트워크
- 같은 네트워크(atdd-net)를 쓰면 container_name으로 접근 가능
- 단, depends_on은 같은 compose 파일 내 서비스만 참조 가능

## Docker Compose 구성

| 파일 | 용도 | 포함 서비스 |
|------|------|------------|
| `docker-compose-infra.yml` | 인프라 | DB (MySQL) |
| `docker-compose.yml` | 서비스 | admin, kiosk, reservation |

## Gradle Task로 인프라 관리

```bash
./gradlew tasks --group=infra
```

| Task | 설명 |
|------|------|
| `infraUp` | 인프라 시작 (DB 등) |
| `infraDown` | 인프라 중지 |
| `servicesUp` | 서비스 시작 (build + up) |
| `servicesDown` | 서비스 중지 |
| `allUp` | 전체 시작 (infra + services) |
| `allDown` | 전체 중지 |

### 구현
```kotlin
// Infra (DB 등)
tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (DB, etc.)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml up -d")
}

tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml down -v")
}

// Services (kiosk, admin, reservation)
tasks.register<Exec>("servicesUp") {
    group = "infra"
    description = "Start services via docker compose (build + up)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml up -d --build")
}

tasks.register<Exec>("servicesDown") {
    group = "infra"
    description = "Stop services and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml down -v")
}

// All (infra + services)
tasks.register("allUp") {
    group = "infra"
    description = "Start all (infra + services)"
    dependsOn("infraUp", "servicesUp")
}

tasks.register("allDown") {
    group = "infra"
    description = "Stop all (infra + services)"
    dependsOn("infraDown", "servicesDown")
}
```

### 장점
- **추상화**: 복잡한 명령을 간단한 task 이름으로
- **CI 편의성**: `./gradlew allUp`으로 통일
- **Task 체이닝**: `dependsOn`, `finalizedBy`로 자동화
- **자기 문서화**: `./gradlew tasks --group=infra`