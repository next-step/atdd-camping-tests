# Step 1: Kiosk Smoke 테스트 확보 가이드

## 목표

kiosk 애플리케이션을 Docker 컨테이너로 기동하고, `atdd-tests` 프로젝트에서 HTTP 200 응답 스모크 테스트 1건을 통과시킨다.

---

## 현재 프로젝트 구조

```
atdd-camping-tests/                    ← 루트 (atdd-tests 프로젝트)
├── build.gradle.kts                   ← Cucumber + RestAssured + JUnit5 테스트 프로젝트
├── settings.gradle                    ← rootProject.name = 'atdd-tests'
├── .gitignore                         ← repos/ 이미 포함됨
├── src/test/
│   ├── java/com/camping/tests/
│   │   ├── RunCucumberTest.java       ← Cucumber Suite Runner
│   │   └── steps/SampleSteps.java     ← 샘플 스텝 (System.out만 출력, 실제 HTTP 호출 없음)
│   └── resources/features/
│       └── sample.feature             ← 샘플 시나리오 (localhost:8080 하드코딩)
├── infra/
│   ├── docker-compose-infra.yml       ← MySQL 인프라 (atdd-net 네트워크)
│   ├── docker-compose.yml             ← ★ 비어 있음 → 작성 필요
│   ├── dockerfiles/
│   │   ├── Dockerfile                 ← 범용 멀티스테이지 Dockerfile (참고용)
│   │   └── Dockerfile-kiosk           ← ★ 비어 있음 → 작성 필요
│   ├── db/init.sql                    ← MySQL 초기 데이터
│   └── wiremock/mappings/
│       └── payment-approve.json       ← WireMock 결제 스텁
└── repos/
    └── atdd-camping-kiosk/            ← 이미 클론됨 (Spring Boot 3.2.0, Java 17)
```

---

## 작업 목록

### 1. 서비스 코드 준비 (repos/ 클론·동기화)

**현황:** `repos/atdd-camping-kiosk/`에 이미 클론되어 있음. `.gitignore`에 `repos/` 포함됨.

**필요 시 재클론 명령:**

```bash
git clone --branch main --single-branch --depth 1 \
  https://github.com/next-step/atdd-camping-kiosk \
  repos/atdd-camping-kiosk
```

**동기화(이후 업데이트):**

```bash
cd repos/atdd-camping-kiosk
git pull --rebase
```

**확인 사항:**

- `.gitignore`에 `repos/`가 이미 등록되어 있어 push 시 자동 제외됨

---

### 2. Dockerfile 작성

| 항목 | 내용 |
|------|------|
| 파일 | `infra/dockerfiles/Dockerfile-kiosk` |
| 현황 | 파일 존재하나 비어 있음 → 내용 작성 필요 |
| 전략 | 멀티스테이지 빌드 (기존 `Dockerfile` 참고) |

**작성 내용:**

```dockerfile
###########
# Builder #
###########
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY . .
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon clean bootJar -x test

############
# Runtime  #
############
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

**핵심 포인트:**

- kiosk 소스 디렉토리 전체가 빌드 컨텍스트로 들어감 (compose의 `context`에서 지정)
- `-x test`로 테스트 스킵 (외부 서비스 의존성 때문에 빌드 시 테스트 실패 방지)
- `$JAVA_OPTS` 환경변수로 JVM 옵션 주입 가능

---

### 3. Docker Compose 작성 (단일 서비스)

| 항목 | 내용 |
|------|------|
| 파일 | `infra/docker-compose.yml` |
| 현황 | 파일 존재하나 비어 있음 → 내용 작성 필요 |

**작성 내용:**

```yaml
services:
  kiosk:
    build:
      context: ../../repos/atdd-camping-kiosk
      dockerfile: ../../infra/dockerfiles/Dockerfile-kiosk
    container_name: atdd-kiosk
    ports:
      - "18081:8080"
    environment:
      - SERVER_PORT=8080
      - SPRING_PROFILES_ACTIVE=default
```

**설정 요약:**

| 항목 | 값 | 설명 |
|------|-----|------|
| 호스트 포트 | `18081` | 로컬 8080 충돌 방지 |
| 컨테이너 포트 | `8080` | kiosk application.yml 기본 포트 |
| build context | `../../repos/atdd-camping-kiosk` | 클론된 소스 루트 |
| dockerfile | `../../infra/dockerfiles/Dockerfile-kiosk` | 별도 Dockerfile 경로 |

**kiosk 앱 특성 (DB 불필요):**

- `application.yml`에서 `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration` 이미 exclude 처리됨
- DB 없이 독립 기동 가능
- `GET /health` 엔드포인트가 외부 서비스 의존 없이 `"OK"` 반환

---

### 4. 기동 및 종료 자동화 (Gradle Task)

**파일:** `build.gradle.kts` (루트)에 task 추가

**추가할 Gradle Task:**

```kotlin
tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Run kiosk via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("kioskComposeDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}
```

**수동 실행 명령 (참고):**

```bash
# 기동 (이미지 빌드 포함)
docker compose -f infra/docker-compose.yml up -d --build

# 상태 확인
docker compose -f infra/docker-compose.yml ps
docker logs atdd-kiosk --tail 100

# 종료
docker compose -f infra/docker-compose.yml down -v
```

---

### 5. Smoke 테스트 작성·실행

**테스트 대상 엔드포인트:**

- `GET /health` → 200 응답, 본문 `"OK"`
- 외부 서비스(Admin/Payment) 의존 없이 항상 정상 응답

**베이스 URL 외부화:**

- 환경변수: `KIOSK_BASE_URL` (기본값: `http://localhost:18081`)
- 시스템 프로퍼티: `-Dkiosk.base-url=http://localhost:18081`

**Cucumber Feature 파일** (`src/test/resources/features/kiosk-smoke.feature`):

```gherkin
# language: ko
기능: Kiosk 서비스 Smoke 테스트

  시나리오: 헬스 체크 - kiosk 서비스가 정상 기동되었는지 확인
    만약 kiosk 서비스가 준비될 때까지 대기한다
    그러면 kiosk 헬스 체크가 200 응답을 반환한다
```

**Step 구현** (`src/test/java/com/camping/tests/steps/KioskSmokeSteps.java`):

- `KIOSK_BASE_URL` 환경변수에서 베이스 URL 읽기 (기본값 `http://localhost:18081`)
- 준비 대기: 짧은 간격(1~2초)으로 `/health` 폴링, 최대 30초 리트라이
- RestAssured로 `GET /health` 호출 → 200 상태코드 검증

**테스트 실행:**

```bash
# 환경변수 주입하여 실행
KIOSK_BASE_URL=http://localhost:18081 ./gradlew test

# 또는 시스템 프로퍼티로
./gradlew test -Dkiosk.base-url=http://localhost:18081
```

---

## 전체 워크플로우 (원커맨드 시퀀스)

```bash
# 1. 기동
./gradlew kioskComposeUp

# 2. 테스트 (컨테이너 ready 대기 포함)
KIOSK_BASE_URL=http://localhost:18081 ./gradlew test

# 3. 종료
./gradlew kioskComposeDown
```

---

## 완료 기준 체크리스트

| # | 기준 | 확인 방법 |
|---|------|----------|
| 1 | `repos/atdd-camping-kiosk` 최신 main 브랜치 동기화 | `git -C repos/atdd-camping-kiosk log --oneline -1` |
| 2 | `infra/dockerfiles/Dockerfile-kiosk` 작성 완료 | 파일 내용 확인 |
| 3 | `infra/docker-compose.yml` kiosk 서비스 정의 | 파일 내용 확인 |
| 4 | `docker compose up` 후 kiosk 컨테이너 정상 기동 | `curl http://localhost:18081/health` → `OK` |
| 5 | Smoke 테스트 200 통과 | `./gradlew test` 성공 |
| 6 | Gradle Task로 기동/종료 자동화 | `./gradlew kioskComposeUp` / `kioskComposeDown` |
| 7 | 포트 충돌 없음 | 호스트 `18081` 사용 (로컬 8080과 분리) |
| 8 | `.gitignore`에 `repos/` 포함 | 이미 등록됨 |

---

## 참고: kiosk 앱 주요 정보

| 항목 | 값 |
|------|-----|
| 프레임워크 | Spring Boot 3.2.0 |
| Java 버전 | 17 |
| 빌드 도구 | Gradle 8.14 |
| 컨테이너 내 포트 | 8080 |
| Health 엔드포인트 | `GET /health` → `"OK"` (200) |
| DB 필요 여부 | 불필요 (AutoConfig exclude 처리됨) |
| 외부 서비스 의존 | Admin(8080), Payment(9090) — smoke 테스트에는 불필요 |