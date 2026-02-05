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
│   ├── db/init.sql           # DB 초기화 스크립트
│   └── wiremock/             # WireMock 스텁 정의
│       └── mappings/         # 결제 API Mock
├── repos/                     # 서브 프로젝트 (gitignore)
│   ├── atdd-camping-kiosk/
│   ├── atdd-camping-admin/
│   └── atdd-camping-reservation/
└── src/test/                  # 테스트 코드
    ├── java/com/camping/tests/
    │   ├── config/           # 테스트 설정 (TestConfig)
    │   ├── dto/              # 테스트용 DTO
    │   ├── hooks/            # Cucumber Hooks (DB 초기화)
    │   └── steps/            # Cucumber Step 정의
    └── resources/features/   # Gherkin Feature 파일
```

## 기술 스택

- **테스트**: Cucumber 7.14.0, RestAssured 5.3.2, JUnit 5, AssertJ 3.25.3
- **앱**: Spring Boot 3.2.0, Java 17
- **인프라**: Docker, MySQL 8.0, WireMock 3.5.4

---

## Step 1: 단일 서비스 Smoke 테스트

### 실행 방법

```bash
# 한 줄로 실행
./gradlew composeUp waitForServices smokeTest

# 또는 단계별 실행
./gradlew composeUp
./gradlew waitForServices
./gradlew smokeTest
./gradlew composeDown
```

### 테스트 내용

- Kiosk, Admin, Reservation 헬스체크
- 각 서비스별 응답 확인 (Kiosk: 200, Admin: 401, Reservation: 200)

### 주요 Gradle Task

| Task | 설명 |
|------|------|
| `./gradlew composeUp` | 모든 앱 서비스 기동 |
| `./gradlew composeDown` | 모든 앱 서비스 종료 |
| `./gradlew composePs` | 컨테이너 상태 확인 |
| `./gradlew waitForServices` | 전체 서비스 준비 대기 |
| `./gradlew smokeTest` | Smoke 테스트 실행 |

---

## Step 2: 다중 서비스 E2E 테스트

### 실행 방법

```bash
# 한 줄로 실행 (전체 초기화 + 테스트)
./gradlew composeDown infraDown infraUp waitForInfra composeUp waitForServices e2eTest

# 또는 단계별 실행
./gradlew infraUp waitForInfra
./gradlew composeUp waitForServices
./gradlew e2eTest
./gradlew composeDown infraDown
```

### 테스트 내용

- **Smoke 테스트**: Kiosk, Admin, Reservation 헬스체크
- **E2E 테스트**: Kiosk → Admin → DB 상품 목록 조회

---

## Step 3: 외부 시스템 격리 (WireMock 결제 연동)

### 실행 방법

```bash
# 한 줄로 실행 (권장)
./gradlew composeDown infraDown infraUp waitForInfra composeUp waitForServices e2eTest

# 또는 단계별 실행
./gradlew infraUp waitForInfra      # MySQL 기동
./gradlew composeUp waitForServices  # 서비스 기동 (Kiosk, Admin, Reservation, payments-mock)
./gradlew e2eTest                    # E2E 테스트 실행
./gradlew composeDown infraDown      # 전체 종료
```

### 테스트 내용

- **Smoke 테스트**: 전체 서비스 헬스체크
- **상품 E2E**: Kiosk → Admin → DB 상품 조회
- **결제 E2E**:
  - 결제 생성 성공 (Kiosk → WireMock)
  - 결제 확정 성공 (Kiosk → WireMock → Admin)
  - 결제 확정 실패 (금액 불일치)

### 아키텍처

```
[테스트 코드] ──HTTP──→ [Kiosk :18081]
                             │
                    ┌────────┼────────┐
                    ▼        ▼        ▼
              [Admin :18082] │  [WireMock :18090]
                    │        │     (payments-mock)
                    ▼        │
               [MySQL :3306] │
```

### 주요 Gradle Task

| Task | 설명 |
|------|------|
| `./gradlew infraUp` | MySQL DB 기동 |
| `./gradlew infraDown` | MySQL DB 종료 |
| `./gradlew infraPs` | DB 상태 확인 |
| `./gradlew waitForInfra` | DB 준비 대기 |
| `./gradlew composeUp` | 전체 앱 서비스 기동 (Kiosk, Admin, Reservation, payments-mock) |
| `./gradlew composeDown` | 전체 앱 서비스 종료 |
| `./gradlew waitForServices` | 전체 서비스 준비 대기 (payments-mock 포함) |
| `./gradlew smokeTest` | Smoke 테스트 실행 |
| `./gradlew e2eTest` | E2E 테스트 실행 (상품 + 결제) |

---

## 포트 정리

| 서비스 | 포트 | URL |
|--------|------|-----|
| MySQL | 3306 | - |
| Kiosk | 18081 | http://localhost:18081 |
| Admin | 18082 | http://localhost:18082 |
| Reservation | 18083 | http://localhost:18083 |
| WireMock (payments-mock) | 18090 | http://localhost:18090 |

---

## 환경변수

테스트 코드에서 사용하는 환경변수 (기본값 설정됨):

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| KIOSK_BASE_URL | http://localhost:18081 | Kiosk 서비스 URL |
| ADMIN_BASE_URL | http://localhost:18082 | Admin 서비스 URL |
| RESERVATION_BASE_URL | http://localhost:18083 | Reservation 서비스 URL |
| DB_URL | jdbc:mysql://localhost:3306/atdd | MySQL 접속 URL |
| DB_USER | root | MySQL 사용자 |
| DB_PASSWORD | secret | MySQL 비밀번호 |

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
docker logs atdd-reservation
docker logs atdd-payments-mock
docker logs atdd-db
```

### MySQL 직접 접속

```bash
docker exec -it atdd-db mysql -uroot -psecret atdd
```

### WireMock 스텁 확인

```bash
# 등록된 스텁 목록 확인
curl http://localhost:18090/__admin/mappings | python3 -m json.tool

# WireMock 헬스체크
curl http://localhost:18090/__admin/health

# 요청 기록 확인
curl http://localhost:18090/__admin/requests
```

### 데이터베이스 스키마 확인

```bash
docker exec -it atdd-db mysql -uroot -psecret atdd -e "SHOW TABLES;"
docker exec -it atdd-db mysql -uroot -psecret atdd -e "SELECT * FROM products;"
```

---

## 핵심 설계

### TestConfig를 통한 URL 중앙 관리

모든 서비스 URL은 [TestConfig.java](src/test/java/com/camping/tests/config/TestConfig.java)에서 중앙 관리됩니다.

### DatabaseHooks를 통한 테스트 격리

각 E2E 테스트 실행 전 [DatabaseHooks.java](src/test/java/com/camping/tests/hooks/DatabaseHooks.java)가 자동으로:
1. 모든 테이블 데이터 삭제 (TRUNCATE)
2. 테스트용 초기 데이터 삽입

### 스키마 관리

- **테이블 생성**: Admin/Reservation 서비스의 Hibernate (`ddl-auto=create`)
- **데이터 초기화**: DatabaseHooks (TRUNCATE + 시드 데이터)

---

## 참고 문서

- [프로젝트 분석](docs/PROJECT-ANALYSIS.md)
- [Step 3 구현 가이드](docs/STEP3-GUIDE.md)
