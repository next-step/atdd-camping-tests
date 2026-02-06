# 인증 및 실행 가이드

## 인증 체계

### Admin 서비스

JWT 기반 인증을 사용합니다.

#### 토큰 획득

```bash
curl -X POST http://localhost:18080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

응답:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

#### 인증된 요청

```bash
curl http://localhost:18080/admin/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 테스트 코드에서의 인증

`AdminClient`가 자동으로 로그인을 처리합니다:

```java
AdminClient adminClient = new AdminClient();
adminClient.createProduct("텐트", 50000);  // 자동 로그인 후 상품 생성
```

## 자격 증명 설정

### 설정 우선순위

```
1. 환경변수     → ADMIN_USERNAME, ADMIN_PASSWORD
2. 시스템 프로퍼티 → -Dadmin.username=...
3. test.properties → admin.username=...
```

### 로컬 개발 환경

`src/test/resources/test.properties`:
```properties
admin.username=admin
admin.password=admin123
```

### CI/CD 환경

환경변수로 주입:
```bash
ADMIN_USERNAME=admin \
ADMIN_PASSWORD=secure_password \
./gradlew test
```

### Docker Compose 환경

`infra/.env`:
```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

## 실행 환경별 가이드

### 로컬 개발

```bash
# 1. 전체 환경 기동
./gradlew allUp

# 2. 서비스 상태 확인
curl http://localhost:18080/login  # Admin
curl http://localhost:18081        # Kiosk
curl http://localhost:19090/__admin/mappings  # WireMock

# 3. 테스트 실행
./gradlew test

# 4. 정리
./gradlew allDown
```

### CI/CD 파이프라인

```yaml
# GitHub Actions 예시
jobs:
  e2e-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Start infrastructure
        run: ./gradlew allUp

      - name: Wait for services
        run: sleep 30

      - name: Run E2E tests
        env:
          ADMIN_USERNAME: ${{ secrets.ADMIN_USERNAME }}
          ADMIN_PASSWORD: ${{ secrets.ADMIN_PASSWORD }}
        run: ./gradlew test

      - name: Cleanup
        if: always()
        run: ./gradlew allDown
```

### 태그별 실행

```bash
# 스모크 테스트 (빠른 검증)
./gradlew test -Dcucumber.filter.tags="@smoke"

# E2E 테스트
./gradlew test -Dcucumber.filter.tags="@e2e"

# 인수 테스트
./gradlew test -Dcucumber.filter.tags="@acceptance"

# AI 후보 시나리오만
./gradlew test -Dcucumber.filter.tags="@ai-candidate"

# 결제 관련만
./gradlew test -Dcucumber.filter.tags="@payment"

# WIP 제외
./gradlew test -Dcucumber.filter.tags="not @wip"

# 복합 조건
./gradlew test -Dcucumber.filter.tags="@acceptance and not @ai-candidate"
```

## 서비스 연결 정보

### 외부 접근 (테스트 코드 → 서비스)

| 서비스 | URL | 설정 키 |
|--------|-----|---------|
| Admin | http://localhost:18080 | `admin.base-url` |
| Kiosk | http://localhost:18081 | `kiosk.base-url` |
| Reservation | http://localhost:18082 | `reservation.base-url` |
| Payments | http://localhost:19090 | `payments.base-url` |

### 내부 통신 (서비스 → 서비스)

Docker 네트워크 내에서의 통신:

| 출발 | 도착 | URL |
|------|------|-----|
| Kiosk | Admin | http://admin:8080 |
| Kiosk | Payments | http://payments:8080 |
| Reservation | Admin | http://admin:8080 |

## 트러블슈팅

### 서비스 연결 실패

```
org.awaitility.core.ConditionTimeoutException
```

**원인**: 서비스가 아직 준비되지 않음

**해결**:
```bash
# 서비스 상태 확인
docker ps

# 로그 확인
docker logs infra-kiosk-1
docker logs infra-admin-1
```

### 인증 실패

```
401 Unauthorized
```

**원인**: 잘못된 자격 증명 또는 토큰 만료

**해결**:
```bash
# 자격 증명 확인
echo $ADMIN_USERNAME
echo $ADMIN_PASSWORD

# 토큰 재발급
curl -X POST http://localhost:18080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### WireMock 스텁 미적용

```
404 Not Found (WireMock)
```

**원인**: 스텁 매핑 파일 미로드

**해결**:
```bash
# 스텁 목록 확인
curl http://localhost:19090/__admin/mappings

# 컨테이너 재시작
docker restart infra-payments-1
```

### DB 연결 실패

```
Communications link failure
```

**원인**: DB가 준비되지 않음

**해결**:
```bash
# DB 상태 확인
docker logs atdd-db

# DB 먼저 기동 후 앱 기동
./gradlew infraUp
sleep 10
./gradlew servicesUp
```

## 보안 주의사항

### 하드코딩 금지

```java
// 잘못된 예시
String password = "admin123";  // 하드코딩

// 올바른 예시
String password = TestConfig.getAdminPassword();  // 외부화
```

### 민감 정보 로깅 금지

```java
// 잘못된 예시
log.info("Token: " + token);  // 토큰 노출

// 올바른 예시
log.info("Authentication successful");  // 민감 정보 제외
```

### Git 커밋 제외

`.gitignore`에 포함되어야 할 항목:
```
infra/.env
*.secret
credentials.json
```