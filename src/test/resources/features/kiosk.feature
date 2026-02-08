# language: ko
기능: Kiosk 서비스 smoke 테스트

  시나리오: Kiosk 서비스 헬스 체크
    만약 "http://localhost:18081/health"에 요청을 보낸다
    그러면 성공 응답을 받는다
