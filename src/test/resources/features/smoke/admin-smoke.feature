#language: ko
기능: Admin 서비스 smoke 테스트

  시나리오: Admin 서비스 health check
    만약 관리자 서비스의 "/login"에 GET 요청을 보낸다
    그러면 성공 응답을 받는다