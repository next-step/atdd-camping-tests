# 인수테스트 레포지토리

## 역할
`admin`, `kiosk`, `reservation` 의 통합 테스트를 제공합니다. 

## 구동방식
1. `./gradlew build` 실행시 `/repos` 하위에 `atdd-camping-kiosk` 프로젝트가 클론됩니다.

## 🐳 Kiosk 애플리케이션 실행

### 🚀 자동화 스크립트 사용 (권장)

프로젝트 루트에서 제공되는 자동화 스크립트를 사용하여 간편하게 애플리케이션을 관리할 수 있습니다.

#### 애플리케이션 시작
```bash
./start-kiosk.sh
```
- 이미지 자동 빌드 (`--build` 포함)
- Docker Compose로 서비스 시작
- 헬스체크 자동 확인
- 애플리케이션 접속 가능 여부 테스트
- 상세한 상태 정보 출력

#### 상태 확인
```bash
./status-kiosk.sh
```
- Docker Compose 서비스 상태 확인
- 컨테이너 리소스 사용량 모니터링
- 애플리케이션 접속 테스트 (http://localhost:8080)
- 최근 로그 (50줄) 출력
- 네트워크 상태 확인
- 유용한 명령어 가이드

#### 애플리케이션 종료
```bash
./stop-kiosk.sh
```
- Docker Compose 서비스 중지
- 컨테이너 및 볼륨 제거
- 네트워크 정리
- 완전한 정리 상태 검증

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

### 🛠️ 문제 해결

#### 포트 충돌 문제
```bash
# 8080 포트를 사용하는 프로세스 확인
lsof -i :8080

# 해당 프로세스 종료 후 재시작
./stop-kiosk.sh
./start-kiosk.sh
```

#### 로그 확인
```bash
# 실시간 로그 확인
docker logs camping-kiosk -f

# 전체 로그 확인
docker logs camping-kiosk
```

#### 컨테이너 접속
```bash
# 컨테이너 내부 접속
docker exec -it camping-kiosk /bin/bash
```