#language: ko
기능: Reservation 서비스 smoke 테스트

  시나리오: Reservation 서비스 health check
    만약 예약 서비스의 "/"에 GET 요청을 보낸다
    그러면 성공 응답을 받는다