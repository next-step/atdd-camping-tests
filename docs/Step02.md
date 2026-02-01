# 명령어 
## 1. 기존 볼륨 삭제 (init.sql은 첫 시작 시에만 실행)
docker compose -f infra/docker-compose-infra.yml down -v

## 2. infra 기동 & DB healthy 대기
docker compose -f infra/docker-compose-infra.yml up -d --wait

## 3. 앱 기동
docker compose -f infra/docker-compose.yml up -d                                                                                                       


# 테스트 데이터 초기화 전략
- seed.sql: 공통 데이터
  - TRUNCATE all tables 
  - seed.sql 실행 
- @api 테스트: API로 데이터 설정 (테스트 독립성)
  - 시나리오가 필요한 데이터에 명시적으로 선언 
