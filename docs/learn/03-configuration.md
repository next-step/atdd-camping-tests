# 설정 관리

## Externalized Configuration (외부화된 설정)
- 12-Factor App의 Factor III: Config 원칙
- 우선순위: 환경변수 → 시스템 프로퍼티 → properties 파일
- 로컬/CI/프로덕션 환경마다 코드 변경 없이 다른 설정 적용 가능
- `TestConfig.java`에서 구현

## Single Source of Truth
- DB 자격증명 등을 `.env` 파일 하나로 관리
- docker-compose와 테스트 코드가 같은 설정 참조