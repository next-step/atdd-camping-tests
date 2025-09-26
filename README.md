# 인수테스트 레포지토리

## 역할
`admin`, `kiosk`, `reservation` 의 통합 테스트를 제공합니다. 

## 구동방식
1. `./gradlew build` 실행시 `/repos` 하위에 `atdd-camping-kiosk` 프로젝트가 클론됩니다.

## 🐳 캠핑 서비스 애플리케이션 실행

### 🚀 통합 서비스 관리 스크립트 사용 (권장)

프로젝트 루트에서 제공되는 `manage-services.sh` 스크립트를 사용하여 모든 서비스를 통합 관리할 수 있습니다.

#### 도움말 확인

```bash
./manage-services.sh help
```

#### 모든 서비스 관리
```bash
# 모든 서비스 시작 (kiosk, admin, reservation)
./manage-services.sh start

# 모든 서비스 상태 확인
./manage-services.sh status

# 모든 서비스 중지
./manage-services.sh stop

# 모든 서비스 재시작
./manage-services.sh restart

# 모든 서비스 로그 확인
./manage-services.sh logs
```

#### 개별 서비스 관리

```bash
# 키오스크 서비스만 시작
./manage-services.sh start kiosk

# 관리자 서비스 상태 확인
./manage-services.sh status admin

# 예약 서비스 중지
./manage-services.sh stop reservation

# 키오스크 로그 확인
./manage-services.sh logs kiosk

# 관리자 서비스 재시작
./manage-services.sh restart admin
```

#### 주요 기능
- 이미지 자동 빌드 (`--build` 포함)
- Docker Compose로 서비스 시작
- 헬스체크 자동 확인
- 애플리케이션 접속 가능 여부 테스트
- 상세한 상태 정보 출력
- 컬러 출력으로 가독성 향상

### 🔧 수동 Docker Compose 사용

자동화 스크립트 대신 직접 Docker Compose 명령어를 사용할 수도 있습니다.

#### 애플리케이션 기동 (이미지 빌드 포함)
```bash
docker compose -f infra/docker-compose.yml up -d --build
```

#### 상태 확인
```bash
# 서비스 상태
docker compose -f infra/docker-compose.yml ps

# 로그 확인
docker logs camping-kiosk --tail 100
```

#### 종료 (컨테이너 및 볼륨 제거)
```bash
docker compose -f infra/docker-compose.yml down -v
```

### 🌐 애플리케이션 접속

애플리케이션이 성공적으로 시작되면 다음 URL에서 접속할 수 있습니다:
- **키오스크 웹 애플리케이션**: http://localhost:8080
- **관리자 웹 애플리케이션**: http://localhost:8081
- **예약 웹 애플리케이션**: http://localhost:8082

## 🧪 테스트 실행

### 전체 테스트 실행
```bash
./gradlew test
```

### 개별 서비스 테스트 실행
```bash
# 키오스크 테스트만 실행
./gradlew test --tests "*kiosk*"

# 관리자 테스트만 실행
./gradlew test --tests "*admin*"

# 예약 테스트만 실행
./gradlew test --tests "*reservation*"
```

### 테스트 보고서 확인
테스트 실행 후 다음 경로에서 상세한 보고서를 확인할 수 있습니다:
- **HTML 보고서**: `build/reports/tests/test/index.html`
- **XML 결과**: `build/test-results/test/`

## 🚀 빠른 시작 가이드

전체 환경을 처음부터 설정하고 테스트를 실행하는 방법:

```bash
# 1. 모든 서비스 시작
./manage-services.sh start

# 2. 서비스 상태 확인
./manage-services.sh status

# 3. 테스트 실행
./gradlew test

# 4. 정리 (선택사항)
./manage-services.sh stop
```

### 개별 서비스만 사용하는 경우:

```bash
# 키오스크만 시작하여 테스트
./manage-services.sh start kiosk
./gradlew test --tests "*kiosk*"
./manage-services.sh stop kiosk
```

### ✅ 성공 기준

프로젝트가 올바르게 설정되었다면 다음 조건들이 모두 만족되어야 합니다:

1. **로컬에서 모든 서비스 컨테이너가 기동되어 접근 가능하다**
    - `./manage-services.sh status` 실행 시 모든 애플리케이션이 정상 응답
    - http://localhost:8080 (키오스크) 접속 가능
    - http://localhost:8081 (관리자) 접속 가능
    - http://localhost:8082 (예약) 접속 가능

2. **atdd-tests의 모든 smoke 테스트가 200 응답으로 통과한다**
   - `./gradlew test` 실행 시 모든 테스트 통과
   - 각 서비스별 애플리케이션 응답성 확인 시나리오 성공

3. **구성과 실행 방법이 자동화되어 있고, 재현이 가능하다**
    - 통합 자동화 스크립트 (`manage-services.sh`) 제공
   - 이 README.md 문서를 통한 완전한 재현 가능

### 🛠️ 문제 해결

#### 포트 충돌 문제
```bash
# 사용 중인 포트 확인
lsof -i :8080  # 키오스크
lsof -i :8081  # 관리자
lsof -i :8082  # 예약

# 모든 서비스 중지 후 재시작
./manage-services.sh stop
./manage-services.sh start
```

#### 로그 확인
```bash
# 모든 서비스 로그 확인
./manage-services.sh logs

# 개별 서비스 로그 확인
./manage-services.sh logs kiosk
./manage-services.sh logs admin
./manage-services.sh logs reservation

# 실시간 로그 확인 (Docker 명령어 직접 사용)
docker logs camping-kiosk -f
docker logs camping-admin -f
docker logs camping-reservation -f
```

#### 컨테이너 접속
```bash
# 각 컨테이너 내부 접속
docker exec -it camping-kiosk /bin/bash
docker exec -it camping-admin /bin/bash
docker exec -it camping-reservation /bin/bash
```

#### 테스트 실패 시
```bash
# 애플리케이션이 준비되지 않은 경우
./manage-services.sh status  # 상태 확인
./manage-services.sh restart # 재시작

# 개별 서비스 재시작
./manage-services.sh restart kiosk

# 테스트 재실행
./gradlew clean test
```