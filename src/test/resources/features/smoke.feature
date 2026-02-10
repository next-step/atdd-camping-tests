# language: ko
기능: 서비스 Smoke 테스트

  시나리오 개요: 헬스 체크 - 서비스가 정상 기동되었는지 확인
    그러면 "<서비스>" 서비스의 "/health" 엔드포인트가 성공 응답을 반환한다

  예:
    | 서비스       |
    | kiosk        |
    | admin        |
    | reservation  |