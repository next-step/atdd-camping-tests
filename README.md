# ATDD Camping Tests

ATDD 기반 캠핑 예약 시스템 테스트 프로젝트

## 프로젝트 구조

```
atdd-camping-tests/
├── build.gradle.kts           # 테스트 프로젝트 빌드 설정
├── infra/                     # 인프라 설정
│   ├── docker-compose-infra.yml   # DB 인프라
│   ├── docker-compose.yml         # 앱 서비스
│   ├── dockerfiles/           # Dockerfile 모음
│   └── db/init.sql           # DB 초기화 스크립트
├── repos/                     # 서브 프로젝트 (gitignore)
│   ├── atdd-camping-kiosk/
│   ├── atdd-camping-admin/
│   └── atdd-camping-reservation/
└── src/test/                  # 테스트 코드
    ├── java/com/camping/tests/
    │   └── steps/            # Cucumber Step 정의
    └── resources/features/   # Gherkin Feature 파일
```

## 기술 스택

- **테스트**: Cucumber 7.14.0, RestAssured 5.3.2, JUnit 5
- **앱**: Spring Boot 3.2.0, Java 17
- **인프라**: Docker, MySQL 8.0

---

## Step 1: 단일 서비스 Smoke 테스트

### 실행 방법

```bash
# 한 줄로 실행
./gradlew composeUp waitForKiosk smokeTest

# 또는 단계별 실행
./gradlew composeUp
./gradlew waitForKiosk
./gradlew smokeTest
./gradlew composeDown
```

### 테스트 내용

- Kiosk 서비스 헬스체크 (GET `/`)
- 200 응답 확인

### 사용 가능한 Gradle Task

| Task | 설명 |
|------|------|
| `./gradlew composeUp` | Kiosk 컨테이너 기동 |
| `./gradlew composeDown` | Kiosk 컨테이너 종료 |
| `./gradlew composePs` | 컨테이너 상태 확인 |
| `./gradlew composeLogs` | Kiosk 로그 확인 |
| `./gradlew waitForKiosk` | Kiosk 서비스 준비 대기 |
| `./gradlew smokeTest` | Smoke 테스트 실행 |

---

## Step 2: 다중 서비스 E2E 테스트

### 실행 방법

```bash
# 한 줄로 실행
./gradlew infraUp waitForInfra composeUp waitForServices multiSmokeTest e2eTest

# 또는 단계별 실행
./gradlew infraUp waitForInfra
./gradlew composeUp waitForServices
./gradlew multiSmokeTest
./gradlew e2eTest
./gradlew composeDown infraDown
```

### 테스트 내용

- **Smoke 테스트**: Kiosk, Admin, Reservation 헬스체크
- **E2E 테스트**: Kiosk → Admin → DB 상품 목록 조회

### 사용 가능한 Gradle Task

| Task | 설명 |
|------|------|
| `./gradlew infraUp` | MySQL DB 기동 |
| `./gradlew infraDown` | MySQL DB 종료 |
| `./gradlew infraPs` | DB 상태 확인 |
| `./gradlew waitForInfra` | DB 준비 대기 |
| `./gradlew waitForServices` | 전체 서비스 준비 대기 |
| `./gradlew multiSmokeTest` | 다중 서비스 Smoke 테스트 |
| `./gradlew e2eTest` | E2E 테스트 |
| `./gradlew allTests` | 전체 테스트 실행 |

---

## 포트 정리

| 서비스 | 포트 | URL |
|--------|------|-----|
| MySQL | 3306 | - |
| Kiosk | 18081 | http://localhost:18081 |
| Admin | 18082 | http://localhost:18082 |
| Reservation | 18083 | http://localhost:18083 |

---

## 환경변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| KIOSK_BASE_URL | http://localhost:18081 | Kiosk 서비스 URL |
| ADMIN_BASE_URL | http://localhost:18082 | Admin 서비스 URL |
| RESERVATION_BASE_URL | http://localhost:18083 | Reservation 서비스 URL |

---

## 트러블슈팅

### 포트 충돌

```bash
./gradlew composeDown
./gradlew infraDown
```

### 컨테이너 로그 확인

```bash
docker logs atdd-kiosk
docker logs atdd-admin
docker logs atdd-db
```

### MySQL 직접 접속

```bash
docker exec -it atdd-db mysql -uroot -psecret atdd
```

---

## 참고 문서

- [프로젝트 분석](docs/PROJECT-ANALYSIS.md)
