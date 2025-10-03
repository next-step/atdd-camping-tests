# ATDD Camping - test

이 문서는 **ATDD Camping 프로젝트의 테스트 환경 구성 및 실행 방법**을 안내합니다.
Smoke Test부터 E2E Test까지 단계별로 실행할 수 있습니다.

---

## ✅ 사전 준비
- Docker / Docker Compose 설치
- JDK 17 이상
- Gradle

---

## 📦 프로젝트 구조

### 서비스 구성
- **kiosk**: 키오스크 서비스 (포트 18081)
- **admin**: 관리자 서비스 (포트 18082)
- **reservation**: 예약 서비스 (포트 18083)
- **atdd-db**: MySQL 8.0 (포트 3306)

### 네트워크
- `atdd-net`: 모든 서비스가 공유하는 Docker 네트워크

---

## 🚀 실행 방법

MySQL DB가 `atdd-net` 네트워크에서 기동됩니다.

### 1단계: 서비스 기동

#### Gradle 태스크 사용 (권장)

```bash
# 모든 서비스 Clone → Build → 기동 (기본: main 브랜치)
./gradlew kioskUp

# 특정 브랜치로 테스트 환경 구축
./gradlew kioskUp -Pbranch=develop
./gradlew kioskUp -Pbranch=feature/new-payment

# 전체 서비스 빌드
./gradlew buildAll

# 서비스 상태 확인
./gradlew kioskStatus

```

#### 브랜치 지정 옵션
`-Pbranch=<브랜치명>` 옵션으로 각 서비스의 특정 브랜치를 사용하여 테스트 환경을 구축할 수 있습니다.
- 기본값: `main`
- 예시: `-Pbranch=develop`, `-Pbranch=feature/payment`
 
### 2단계: 테스트 실행

#### Smoke Test
서비스가 정상적으로 응답하는지 확인하는 기본 헬스 체크 테스트

```bash
./gradlew testSmoke
```

**테스트 시나리오:**
- Kiosk 헬스 체크: `GET /`
- Admin 헬스 체크: `GET /admin`
- Reservation 헬스 체크: `GET /`

```bash
./gradlew test
```

**테스트 시나리오:**
- 상품 조회: Admin 인증 → Kiosk 상품 목록 조회

---

2. **서비스 재기동:**
   ```bash
   ./gradlew kioskDown
   ./gradlew kioskUp
   ```

## 🛠️ Gradle 태스크 목록

### Infra 관리
- `cloneAll`: 모든 저장소 Clone/업데이트
- `buildAll`: 모든 서비스 JAR 빌드
- `kioskUp`: Kiosk 서비스 전체 기동 (Clone → Build → Up)
- `kioskDown`: Kiosk Compose 종료 및 볼륨 삭제
- `kioskStatus`: 컨테이너 상태 확인
- `kioskLogs`: Kiosk 로그 확인

### 개별 서비스 관리
- `kioskClone` / `adminClone` / `reservationClone`: 개별 저장소 Clone/업데이트
- `kioskBuild` / `adminBuild` / `reservationBuild`: 개별 서비스 빌드

### 테스트
- `testSmoke`: Smoke Test 실행
- `test`: 전체 테스트 실행 (E2E 포함)

### 프로젝트 구조
- 인프라 태스크는 `gradle/infra-tasks.gradle.kts`에 정의되어 있습니다
- Docker Compose 설정은 `infra/docker-compose.yml`에 정의되어 있습니다

