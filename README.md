# 캠핑장 MSA 시스템 테스트 저장소

## 구성 요소
- infra : MSA 서비스들을 실행하기 위한 Docker 관련 코드들, MSA의 서비스들이 공통적으로 접근하는 DB 및 wiremock 관련 코드들
- repos : MSA를 구성하는 서비스들의 실제 레포 코드

## 테스트 전 환경 구축 방법

테스트를 실행하기 전에 다음 단계를 순서대로 진행하여 개발 환경을 구축합니다.

### 1. MSA 저장소 클론

각 MSA의 소스 코드를 `repos` 디렉토리 아래에 클론하거나 최신 코드로 업데이트합니다.

- **모든 저장소 클론 또는 업데이트**:
  ```bash
  ./gradlew cloneRepositories
  ```

### 2. 인프라 서비스 실행 및 종료

테스트에 필요한 데이터베이스(MySQL) 컨테이너를 관리합니다.

- **인프라 실행**: 아래 명령어를 사용하여 `infra/docker-compose-infra.yml`에 정의된 DB 서비스를 실행합니다.
  ```bash
  ./gradlew startInfraContainer
  ```

- **인프라 종료**: 실행 중인 DB 서비스를 중지하고 컨테이너를 삭제합니다.
  ```bash
  ./gradlew stopInfraContainers
  ```

### 3. 애플리케이션 서비스 실행 및 종료

MSA를 구성하는 애플리케이션 컨테이너를 관리합니다.

- **서비스 실행**: 아래 명령어를 사용하여 `infra/docker-compose.yml`에 정의된 모든 애플리케이션 서비스를 백그라운드에서 실행합니다. **반드시 인프라 서비스가 먼저 실행되어야 합니다.**
  ```bash
  ./gradlew dockerComposeUp
  ```

- **서비스 종료**: 실행 중인 모든 애플리케이션 서비스를 중지하고 컨테이너를 삭제합니다.
  ```bash
  ./gradlew dockerComposeDown
  ```

## 테스트 실행 방법

```bash
 KIOSK_BASE_URL=http://localhost:18080 ADMIN_BASE_URL=http://localhost:18081 RESERVATION_BASE_URL=http://localhost:18082 PAYMENTS_MOCK_DEFAULT_URL=http://localhost:18083 ./gradlew test
```
