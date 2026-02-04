# ATDD Camping Tests

캠핑 예약 마이크로서비스 시스템의 ATDD(Acceptance Test-Driven Development) 테스트 프로젝트.
Cucumber BDD + RestAssured로 Docker 컨테이너 위의 서비스들을 검증한다.

## 사전 요구사항

- **Java 17** 이상
- **Docker Desktop** (Docker Compose 포함)
- **Git**

## 프로젝트 구조

```
atdd-camping-tests/
├── .env                          # 포트, DB 접속 정보 등 환경변수 중앙 관리
├── build.gradle.kts              # Gradle 빌드 설정 + Docker 태스크 정의
├── infra/
│   ├── docker-compose-infra.yml  # 인프라 (MySQL, atdd-net 네트워크)
│   ├── docker-compose.yml        # 앱 서비스 (kiosk, admin, reservation)
│   ├── dockerfiles/              # 각 서비스 Dockerfile
│   └── db/init.sql               # DB 스키마 생성 + 시드 데이터
├── repos/                        # 마이크로서비스 소스코드 (git-ignored)
│   ├── atdd-camping-kiosk/
│   ├── atdd-camping-admin/
│   └── atdd-camping-reservation/
└── src/test/
    ├── resources/features/       # Gherkin 시나리오 (.feature, 한국어)
    └── java/com/camping/tests/   # Step 정의 + 테스트 러너
```

## 서비스 소스코드 준비

`repos/` 디렉토리에 3개 서비스를 클론한다:

```bash
mkdir -p repos
git clone <kiosk-repo-url>       repos/atdd-camping-kiosk
git clone <admin-repo-url>       repos/atdd-camping-admin
git clone <reservation-repo-url> repos/atdd-camping-reservation
```

## 실행 방법

### 1단계: 인프라 기동 (MySQL + 네트워크)

```bash
./gradlew infraUp
```

- MySQL 8.0 컨테이너(`atdd-db`) 기동
- `atdd-net` Docker 네트워크 생성
- `infra/db/init.sql` 자동 실행 (테이블 생성 + 시드 데이터)

### 2단계: DB 준비 확인

```bash
docker ps
```

`atdd-db`의 STATUS가 `(healthy)`인지 확인한다. healthy가 되기 전에 앱을 띄우면 DB 연결 실패.

```
NAMES       STATUS
atdd-db     Up 30 seconds (healthy)    ← 이 상태여야 함
```

### 3단계: 앱 서비스 기동

```bash
./gradlew composeUp
```

- kiosk (`:18081`), admin (`:18082`), reservation (`:18083`) 컨테이너 빌드 + 기동
- 소스코드 변경 시 자동 재빌드 (`--build` 포함)
- admin, reservation은 공용 MySQL(`atdd-db`)에 연결

### 4단계: 서비스 정상 확인 (선택)

```bash
curl http://localhost:18081/health        # kiosk       → OK
curl http://localhost:18082/health        # admin       → OK
curl http://localhost:18083/health        # reservation → OK
curl http://localhost:18081/api/products  # E2E 확인   → JSON 배열
```

### 5단계: 테스트 실행

```bash
./gradlew test
```

### 6단계: 정리

```bash
./gradlew composeDown    # 앱 서비스 종료
./gradlew infraDown      # 인프라 종료 + DB 볼륨 삭제
```

## 전체 명령 요약

```bash
# 기동 → 테스트 → 정리 (한 줄)
./gradlew infraUp && sleep 10 && ./gradlew composeUp && ./gradlew test && ./gradlew composeDown && ./gradlew infraDown
```

## Gradle 태스크 목록

| 태스크 | 설명 |
|--------|------|
| `./gradlew infraUp` | 인프라 기동 (MySQL + atdd-net 네트워크) |
| `./gradlew infraDown` | 인프라 종료 + 볼륨 삭제 |
| `./gradlew composeUp` | 앱 3개 서비스 빌드 + 기동 |
| `./gradlew composeDown` | 앱 3개 서비스 종료 |
| `./gradlew test` | Cucumber 테스트 실행 |
| `./gradlew clean` | 빌드 산출물 삭제 |

## 포트 구성

| 서비스 | 호스트 포트 | 컨테이너 포트 |
|--------|:-----------:|:------------:|
| kiosk | 18081 | 8080 |
| admin | 18082 | 8080 |
| reservation | 18083 | 8080 |
| MySQL | 3306 | 3306 |

포트 변경이 필요하면 `.env` 파일만 수정하면 된다.

## 트러블슈팅

| 증상 | 해결 |
|------|------|
| admin/reservation 기동 실패 (DB 연결) | `docker ps`로 `atdd-db`가 `(healthy)` 상태인지 확인 |
| init.sql이 실행 안 됨 | `./gradlew infraDown`으로 볼륨 삭제 후 재기동 |
| 포트 충돌 | `.env`에서 포트 번호 변경 |
| 이미지가 오래됨 | `./gradlew composeDown` 후 `./gradlew composeUp`으로 재빌드 |
| kiosk에서 상품 조회 실패 (500) | admin 컨테이너 로그 확인: `docker logs atdd-admin` |