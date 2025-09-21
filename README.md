# 캠핑장 MSA 시스템 테스트 저장소

## 구성 요소
- infra : MSA 서비스들을 실행하기 위한 Docker 관련 코드들, MSA의 서비스들이 공통적으로 접근하는 DB 및 wiremock 관련 코드들
- repos : MSA를 구성하는 서비스들의 실제 레포 코드

## 테스트 전 환경 구축 방법
### Docker Compose 서비스 실행 및 종료

- **서비스 실행**: 아래 명령어를 사용하여 `infra/docker-compose.yml`에 정의된 모든 서비스를 백그라운드에서 실행합니다.
  ```bash
  ./gradlew dockerComposeUp
  ```

- **서비스 종료**: 실행 중인 모든 서비스를 중지하고 컨테이너를 삭제합니다.
  ```bash
  ./gradlew dockerComposeDown
  ```

## 테스트 실행 방법

```bash
 KIOSK_BASE_URL=http://localhost:18080 ./gradlew test
```
