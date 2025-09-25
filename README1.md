# ATDD Camping Tests

이 저장소는 ATDD(접근 테스트 주도 개발) 방식으로 키오스크 애플리케이션을 검증하기 위한 테스트 프로젝트입니다. Cucumber(Behave 스타일)와 JUnit Platform을 사용하며, 도커 컴포즈를 통해 키오스크 앱 이미지를 빌드/실행하는 커스텀 Gradle 태스크를 제공합니다.

아래 문서는 다음 내용을 포함합니다.
- 커스텀 Gradle 태스크의 역할과 사용 방법
- 테스트 실행 방법(기본 실행, 필터링 실행, 로컬 실행 순서 예시)


## 사전 요구사항
- Docker 20+ 및 Docker Compose 사용 가능 환경
- Git 설치(리포지토리 클론 태스크 사용 시)
- JDK 17+ (Gradle Wrapper가 자동으로 사용되지만 JDK는 필요)
- 로컬 포트 18081 사용 가능(기본값, 환경변수로 변경 가능)


## Gradle Wrapper로 실행
모든 명령은 프로젝트 루트에서 실행합니다.
- macOS/Linux: `./gradlew <task>`
- Windows: `gradlew <task>`

예) Gradle 버전/환경 확인: `./gradlew -v`


## 커스텀 Gradle 태스크 안내
이 프로젝트에는 도커/설정 관련 커스텀 태스크가 포함되어 있습니다. (정의 파일: `kiosk-tasks.gradle.kts`)

### 1) cloneKioskRepo
- 역할: 서브 디렉터리 `repo/atdd-camping-kiosk`에 키오스크 애플리케이션 리포지토리를 클론하거나, 이미 존재하면 최신 상태로 `git pull` 합니다.
- 그룹: setup
- 사용 예:
  - `./gradlew cloneKioskRepo`

### 2) kioskAppUp
- 역할: `infra/docker-compose.yml`을 사용하여 키오스크 애플리케이션을 빌드 후 백그라운드로 기동합니다. (`docker compose up -d --build`)
- 그룹: docker
- 기본 동작:
  - 컨테이너 이름: `atdd-kiosk`
  - 이미지: `atdd/kiosk:latest`
  - 포트: 호스트 `${KIOSK_PORT:-18081}` → 컨테이너 `8080`
  - 환경 변수:
    - `KIOSK_PORT` (기본 18081)
    - `KIOSK_BASE_URL` (기본 `http://localhost:${KIOSK_PORT}`)
    - `JAVA_OPTS` (Spring Boot 실행 시 JVM 옵션 주입)
- 사용 예:
  - 기본 포트로 기동: `./gradlew kioskAppUp`
  - 포트를 28081로 바꿔 기동: `KIOSK_PORT=28081 ./gradlew kioskAppUp`
  - 베이스 URL 지정: `KIOSK_BASE_URL=http://docker.host:18081 ./gradlew kioskAppUp`

### 3) kioskAppDown
- 역할: `infra/docker-compose.yml` 기준으로 컨테이너들을 종료/정리합니다. (`docker compose down`)
- 그룹: docker
- 사용 예: `./gradlew kioskAppDown`

### 4) ps
- 역할: 현재 컴포즈 상태를 출력합니다. (`docker compose ps`)
- 그룹: docker
- 사용 예: `./gradlew ps`

### 5) logs
- 역할: 키오스크 컨테이너(`atdd-kiosk`)의 최근 100줄 로그를 출력합니다. (내부적으로 `docker logs atdd-kiosk --tail 100`)
- 그룹: docker
- 사용 예: `./gradlew logs`

참고: 인프라(MySQL 등)를 별도 컴포즈로 띄우려면 아래 파일을 직접 사용하세요.
- `infra/docker-compose-infra.yml` (예: `docker compose -f infra/docker-compose-infra.yml up -d`)


## 테스트 실행 방법
테스트는 JUnit Platform + Cucumber 7으로 실행되며, 러너는 `src/test/java/com/camping/tests/RunCucumberTest.java`입니다. 피처 파일은 `src/test/resources/features` 아래에 위치합니다.

### 기본 실행
- `./gradlew test`

### 특정 시나리오 이름으로 필터링 실행
- Cucumber의 이름 필터 사용(정규식/문자열 매칭):
  - `./gradlew test -Dcucumber.filter.name="헬스 체크"`

### 태그로 필터링 실행
- 시나리오/피처에 태그를 달았다면:
  - `./gradlew test -Dcucumber.filter.tags="@smoke"`

### 특정 피처만 실행
- 시스템 속성으로 피처 경로 지정(클래스패스 기준):
  - `./gradlew test -Dcucumber.features=classpath:features/kiosk-smoke.feature`

### 로컬에서 애플리케이션 기동 후 스모크 테스트 순서 예시
1. 키오스크 리포지토리 준비: `./gradlew cloneKioskRepo`
2. 앱 기동(도커): `./gradlew kioskAppUp`
3. 상태 확인(선택): `./gradlew ps`
4. 테스트 실행: `./gradlew test`
5. 로그 확인(필요 시): `./gradlew logs`
6. 종료/정리: `./gradlew kioskAppDown`


## 트러블슈팅
- 포트 충돌: 기본 18081 포트를 다른 프로세스가 사용하는 경우 `KIOSK_PORT` 환경변수로 포트를 바꿔 실행하세요.
- 첫 빌드가 느림: Docker가 내부적으로 앱을 빌드하므로 첫 실행 시 시간이 걸릴 수 있습니다. 캐시가 생기면 이후에는 빨라집니다.
- 로그 확인: `./gradlew logs`로 최근 100줄을 확인할 수 있습니다. 더 많은 로그가 필요하면 직접 `docker logs atdd-kiosk -f`를 사용하세요.
- Git 인증 문제: 사내 네트워크/프록시가 필요한 환경이라면 `cloneKioskRepo` 전에 Git 설정을 완료하세요.