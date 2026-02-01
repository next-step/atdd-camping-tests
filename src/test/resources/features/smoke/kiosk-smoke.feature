#language: ko
기능: Kiosk 서비스 smoke 테스트

  시나리오: Kiosk 서비스 health check
    만약 키오스크 서비스의 "/health"에 GET 요청을 보낸다
    그러면 성공 응답을 받는다
    그리고 응답 본문은 "OK"이다