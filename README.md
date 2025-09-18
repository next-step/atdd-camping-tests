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
2. **cloneKioskRepository()**: GitHub에서 camping kiosk 애플리케이션 복제
   - 저장소: `https://github.com/next-step/atdd-camping-kiosk.git`
   - 위치: `repos/atdd-camping-kiosk`
3. **dockerComposeUp()**: Docker Compose로 전체 환경 실행

### Kiosk 애플리케이션 (in docker-compose.yml)
- port: 8080

## 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test
```
