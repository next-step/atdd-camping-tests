# ATDD 캠핑 예약 시스템 테스트 인프라 구성 및 실행 분석 리포트

## 1. 개요

본 프로젝트는 ATDD(Acceptance Test-Driven Development) 방식으로 구현된 캠핑 예약 시스템의 테스트 인프라입니다. Gradle 빌드 시스템과 Docker 컨테이너를 활용하여 완전한 테스트 환경을 자동으로 구성합니다.

## 2. 프로젝트 구성

### 2.1 기본 설정 (build.gradle.kts)
- **프로젝트**: Java 기반, 그룹: `com.camping`, 버전: `0.0.1-SNAPSHOT`
- **빌드 시스템**: Gradle with Kotlin DSL
- **Java 버전**: Java 17 (CLAUDE.md 참조)

### 2.2 핵심 의존성

#### 테스트 프레임워크
- **Cucumber 7.14.0**: BDD 테스트 프레임워크, 한국어 시나리오 지원
- **JUnit Platform**: 테스트 실행 엔진
  - junit-platform-suite: 1.10.0
  - junit-jupiter-api/engine: 5.10.0

#### API 테스트 도구
- **REST Assured 5.3.2**: RESTful API 테스트 라이브러리
- **Jackson 2.17.2**: JSON 직렬화/역직렬화

#### 검증 및 유틸리티
- **AssertJ 3.27.4**: 유창한 스타일의 검증 라이브러리
- **Lombok 1.18.42**: 코드 생성 도구

#### 데이터베이스 및 로깅
- **MySQL Connector 9.4.0**: 테스트 훅에서 데이터베이스 연결
- **Logback 1.5.18**: 로깅 시스템

## 3. 인프라 구성 자동화 (setup-tasks.gradle.kts)

### 3.1 setupTestInfra 태스크 워크플로우

```
setupTestInfra 실행
├── 1. createReposDirectory()     # repos 디렉토리 생성
├── 2. setupRepository() × 3      # 저장소 복제/업데이트
├── 3. runInfraContainers()      # 인프라 컨테이너 실행
├── 4. waitForInfra()            # 서비스 준비 대기
└── 5. runServiceContainers()    # 서비스 컨테이너 실행
```

### 3.2 외부 저장소 관리

자동으로 복제되는 3개의 서비스 저장소:

| 저장소명 | URL | 브랜치 | 역할 |
|---------|-----|--------|------|
| atdd-camping-kiosk | github.com/next-step/atdd-camping-kiosk | main | 키오스크 서비스 |
| atdd-camping-admin | github.com/ivvve/atdd-camping-admin | mysql | 관리자 서비스 |
| atdd-camping-reservation | github.com/ivvve/atdd-camping-reservation | mysql | 예약 서비스 |

### 3.3 저장소 관리 로직
- **신규**: `git clone --depth 1 --single-branch` 으로 최적화된 복제
- **기존**: `git pull origin [branch]` 로 최신 변경사항 동기화

## 4. Docker 컨테이너 오케스트레이션

### 4.1 2단계 컨테이너 실행 전략

#### 1단계: 인프라 컨테이너 (docker-compose-infra.yml)
```bash
docker-compose -f ./infra/docker-compose-infra.yml up -d
```
- MySQL 데이터베이스
- WireMock (외부 서비스 모킹)

#### 2단계: 서비스 컨테이너 (docker-compose.yml)
```bash
docker-compose -f ./infra/docker-compose.yml up -d
```
- 복제된 애플리케이션 서비스들

### 4.2 서비스 준비 상태 모니터링

#### MySQL 헬스 체크 (`waitForMySql`)
- **연결 정보**: localhost:3306, 데이터베이스: atdd, 계정: root/secret
- **검증 방식**: `SELECT 1` 쿼리 실행
- **재시도**: 최대 30회, 1초 간격
- **드라이버**: MySQL Connector/J 사용

#### WireMock 헬스 체크 (`waitForWireMock`)
- **엔드포인트**: `http://localhost:8090/__admin/mappings`
- **검증 방식**: HTTP 200 상태 코드 확인
- **재시도**: 최대 30회, 1초 간격
- **타임아웃**: HTTP 요청 5초

## 5. 테스트 실행 환경

### 5.1 테스트 실행 설정
```kotlin
tasks.test {
    useJUnitPlatform()
}
```

## 6. 핵심 실행 명령어

### 6.1 초기 환경 구성
```bash
./gradlew setupTestInfra    # 전체 환경 자동 구성
```

### 6.2 테스트 실행
```bash
./gradlew test              # 모든 테스트 실행
./gradlew test --info       # 상세 로그와 함께 테스트 실행
```

### 6.3 인프라 관리
```bash
# 인프라만 실행
docker-compose -f infra/docker-compose-infra.yml up -d

# 전체 스택 실행
docker-compose -f infra/docker-compose.yml up -d

# 인프라 중지
docker-compose -f infra/docker-compose-infra.yml down
docker-compose -f infra/docker-compose.yml down
```

## 7. 기술적 특징

### 7.1 안정성 보장
- 서비스별 헬스 체크를 통한 준비 상태 확인
- 재시도 메커니즘으로 일시적 네트워크 문제 대응
- 실패 시 명확한 오류 메시지와 프로세스 종료

### 7.2 효율성 최적화
- Shallow clone으로 빠른 저장소 복제
- 기존 저장소는 pull로 증분 업데이트
- 단계별 컨테이너 실행으로 의존성 관리

### 7.3 유지보수성
- 저장소 정보를 data class로 구조화
- 함수 단위로 모듈화된 설정 로직
- 명확한 로그 메시지로 진행 상황 추적

## 8. 결론

본 테스트 인프라는 완전 자동화된 ATDD 환경을 제공합니다. 단일 명령어(`./gradlew setupTestInfra`)로 복잡한 마이크로서비스 환경을 구성하고, 안정적인 헬스 체크 메커니즘을 통해 테스트 실행 준비를 보장합니다. 이는 개발자가 인프라 설정에 시간을 소모하지 않고 비즈니스 로직과 테스트 시나리오 작성에 집중할 수 있게 합니다.
