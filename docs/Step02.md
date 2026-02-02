# Step 02: E2E 테스트 환경 구성

## 실행 순서

```bash
# ① 인프라(DB) 기동
docker compose -f infra/docker-compose-infra.yml down -v  # 기존 볼륨 삭제
docker compose -f infra/docker-compose-infra.yml up -d --wait

# ② 앱 기동
docker compose -f infra/docker-compose.yml up -d --build

# ③ E2E 테스트 실행
./gradlew test

# ④ 정리
./gradlew allDown
```

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| admin | 18080 | 관리자 서비스 |
| kiosk | 18081 | 키오스크 서비스 |
| reservation | 18082 | 예약 서비스 |
| atdd-db (MySQL) | 3306 | 데이터베이스 |

## 테스트 데이터 초기화 전략

### @Before("@api") Hook
1. TRUNCATE all tables (FK 체크 비활성화)
2. seed.sql 실행 (공통 데이터)

### 시나리오별 데이터
- `조건` 스텝에서 API로 명시적 설정
- 테스트 독립성 보장