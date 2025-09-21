# Dockerfiles

## Dockerfile-svc

공통 Spring Boot 애플리케이션용 Dockerfile입니다.

### 사용법

```yaml
# docker-compose.yml
services:
  your-service:
    build:
      context: ..
      dockerfile: infra/dockerfiles/Dockerfile-svc
      args:
        SERVICE_NAME: your-service-name
```

### 필수 요구사항

- `SERVICE_NAME` 빌드 인자가 필요합니다
- 서비스 소스는 `infra/repos/atdd-camping-{SERVICE_NAME}/` 경로에 있어야 합니다
- Gradle 기반 Spring Boot 프로젝트여야 합니다

### 새로운 서비스 추가

새로운 Spring Boot 서비스를 추가하려면:

1. `infra/repos/atdd-camping-{서비스명}/` 경로에 소스 배치
2. `docker-compose.yml`에 서비스 정의 추가:
   ```yaml
   new-service:
     build:
       context: ..
       dockerfile: infra/dockerfiles/Dockerfile-svc
       args:
         SERVICE_NAME: new-service
     container_name: atdd-new-service
     ports:
       - "18084:8080"
     networks:
       - atdd-net
     environment:
       <<: *common-spring-env
       SPRING_DATASOURCE_URL: jdbc:h2:mem:newservicedb
   ```
3. `app-convention.gradle.kts`의 `cloneRepoIfNotExists` 호출 추가