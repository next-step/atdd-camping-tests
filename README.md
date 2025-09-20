## 기술 스택

- **Java 17**
- **Cucumber**
- **JUnit 5**
- **REST Assured**

## 인프라 셋업

테스트가 의존하는 인프라를 Docker 기반으로 셋업하는 Gradle 태스크를 제공합니다. 아래 명령어로 실행할 수 있습니다.

```bash
./gradlew setupTestInfra
```

### setupTestInfra task

1. **createReposDirectory()**: `repos` 디렉토리 생성
2. **setupRepository()**: GitHub에서 서비스 애플리케이션 복제
   - `repos/atdd-camping-kiosk` (main branch)
   - `repos/atdd-camping-admin` (mysql branch)
   - `repos/atdd-camping-reservation` (mysql branch)
3. **runInfraContainers()**: MySQL 등 인프라 컨테이너 실행
4. **runServiceContainers()**: 전체 서비스 컨테이너 실행

### MySQL (in docker-compose.yml)
- port: 3306

### Kiosk 애플리케이션 (in docker-compose.yml)
- port: 8080

### Admin 애플리케이션 (in docker-compose.yml)
- port: 8081

### Reservation 애플리케이션 (in docker-compose.yml)
- port: 8082

## 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test
```
