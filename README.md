# ATDD Camping - 통합 인수 테스트

## 1. 개요 (Overview)

이 프로젝트는 MSA(Microservice Architecture)로 구성된 캠핑 예약 시스템의 **통합 인수 테스트**를 자동화하고 관리하기 위한 중앙 리포지토리입니다.

Cucumber, Gradle, Docker Compose를 활용하여 전체 테스트 과정을 자동화하였으며, 단일 명령어를 통해 다음과 같은 일련의 작업을 수행합니다.

- 하위 서비스 프로젝트들의 소스 코드 최신화 (`git pull`)
- Docker Compose를 이용한 테스트 환경(인프라, 서비스) 자동 구성
- 모든 서비스가 정상적으로 실행될 때까지 대기 (Health Checking)
- Cucumber 인수 테스트 실행
- 테스트 종료 후 모든 테스트 환경 자원 정리

## 2. 사전 준비 사항 (Prerequisites)

이 프로젝트를 실행하기 위해서는 아래의 소프트웨어가 설치되어 있어야 합니다.

- **Java (JDK)**: 17 버전 이상
- **Docker & Docker Compose**: 컨테이너 기반의 테스트 환경 구성을 위해 필수
- **Git**: 소스 코드 관리를 위해 필수

## 3. 설치 및 설정 (Setup)

테스트를 실행하기 전, 아래의 설정 과정을 먼저 완료해야 합니다.

### 3.1. 메인 프로젝트 클론

이 `atdd-camping-tests` 리포지토리를 원하는 위치에 클론합니다.

```bash
git clone [atdd-camping-tests 리포지토리 주소]
cd atdd-camping-tests
```

### 3.2. 하위 서비스 프로젝트 클론

프로젝트 루트의 `repos/` 디렉토리 안에 테스트 대상이 되는 마이크로서비스들의 리포지토리를 클론해야 합니다.

```bash
# 예시: admin 서비스 클론
git clone [atdd-camping-admin 리포지토리 주소] repos/atdd-camping-admin

# 예시: kiosk 서비스 클론
git clone [atdd-camping-kiosk 리포지토리 주소] repos/atdd-camping-kiosk

# 예시: reservation 서비스 클론
git clone [atdd-camping-reservation 리포지토리 주소] repos/atdd-camping-reservation
```

모든 설정을 마친 후의 디렉토리 구조는 다음과 같아야 합니다.

```
atdd-camping-tests/
├── repos/
│   ├── atdd-camping-admin/      # Admin 서비스 프로젝트
│   ├── atdd-camping-kiosk/      # Kiosk 서비스 프로젝트
│   └── atdd-camping-reservation/  # Reservation 서비스 프로젝트
├── infra/
├── src/
└── build.gradle.kts
```

## 4. 주요 실행 명령어 (Key Commands)

모든 명령어는 프로젝트의 루트 디렉토리에서 실행합니다.

### 🚀 전체 인수 테스트 자동 실행

가장 핵심적인 명령어로, 아래 명령어 하나로 모든 준비, 실행, 정리 과정을 자동으로 수행합니다.

```bash
./gradlew acceptanceTest
```

위 명령어를 실행하면 내부적으로 다음의 태스크들이 순서대로 동작합니다.
1.  `gitPullAll`: `repos/` 안의 모든 하위 프로젝트에 대해 `git pull`을 실행하여 최신 코드로 업데이트합니다.
2.  `composeUp`: `docker-compose-infra.yml`과 `docker-compose.yml`을 순차적으로 실행하여 전체 테스트 환경을 구성합니다.
3.  `waitForServices`: 모든 서비스 컨테이너가 요청을 받을 준비가 될 때까지 Health Check를 수행하며 대기합니다.
4.  `test`: 모든 서비스가 준비되면 Cucumber 인수 테스트를 실행합니다.
5.  `composeDown`: 테스트가 성공/실패 여부와 관계없이 종료된 후, 모든 Docker 컨테이너와 네트워크를 정리합니다.

### 띄우고 내리기

테스트 환경을 수동으로 제어하고 싶을 때 사용합니다.

- **테스트 환경 시작**:
  ```bash
  ./gradlew composeUp
  ```

- **테스트 환경 종료 및 정리**:
  ```bash
  ./gradlew composeDown
  ```

## 5. 기타 유용한 명령어 (Other Useful Commands)

- **서비스 컨테이너 상태 확인**:
  ```bash
  ./gradlew ServicePs
  ```

- **특정 서비스 로그 실시간 확인 (예: kiosk)**:
  ```bash
  ./gradlew ServiceLog
  ```

- **모든 하위 프로젝트 소스 최신화**:
  ```bash
  ./gradlew gitPullAll
  ```