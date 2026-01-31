# ATDD Camping - Kiosk Smoke Tests

## 개요

이 프로젝트는 `atdd-camping-kiosk` 애플리케이션에 대한 Cucumber 기반의 Smoke 테스트를 포함합니다.

테스트의 목적은 외부 프로세스로 Kiosk 애플리케이션을 실행하고, 간단한 HTTP 호출을 통해 서비스가 정상적으로 기동되었는지 확인하는 것입니다.

## 주요 파일 구성

- **`build.gradle.kts`**: 프로젝트 의존성을 관리하고 Kiosk 애플리케이션을 Docker Compose로 제어하기 위한 Gradle Task(`kioskComposeUp`, `kioskComposeDown`)를 정의합니다.
- **`infra/docker-compose.yml`**: Kiosk 애플리케이션을 서비스로 정의하고 호스트의 `18081` 포트와 컨테이너의 `8080` 포트를 매핑합니다.
- **`src/test/resources/features/kiosk.feature`**: Cucumber 테스트 시나리오를 정의합니다. Kiosk 헬스체크 엔드포인트(`/health`)에 대한 요청과 예상 결과를 기술합니다.
- **`src/test/java/com/camping/tests/steps/KioskSteps.java`**: `kiosk.feature` 파일에 정의된 시나리오의 실제 동작을 구현합니다. RestAssured를 사용하여 HTTP 요청을 보내고 AssertJ로 응답을 검증합니다.

## 설정

테스트는 `KIOSK_BASE_URL` 환경 변수를 사용하여 Kiosk 서비스의 기본 URL을 설정할 수 있습니다. 설정하지 않으면 기본값인 `http://localhost:18081`이 사용됩니다.

```bash
export KIOSK_BASE_URL=http://your-kiosk-host:port
```

## 실행 방법

테스트는 3단계로 진행됩니다.

### 1. Kiosk 애플리케이션 실행

아래 Gradle Task를 실행하여 Docker Compose로 Kiosk 컨테이너를 빌드하고 실행합니다.

```bash
./gradlew kioskComposeUp
```

### 2. Smoke 테스트 실행

Kiosk 애플리케이션이 실행된 상태에서 아래 명령어로 Cucumber 테스트를 실행합니다.

```bash
./gradlew test
```

테스트는 설정된 Kiosk 기본 URL의 `/health` 엔드포인트에 GET 요청을 보내고, HTTP 상태 코드 200과 응답 본문 "OK"를 기대합니다.

### 3. Kiosk 애플리케이션 종료

테스트가 완료된 후, 아래 명령어로 Kiosk 컨테이너를 중지하고 관련 리소스를 삭제합니다.

```bash
./gradlew kioskComposeDown
```
