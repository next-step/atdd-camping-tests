# language: ko
기능: 각 서비스 스모크 테스트

  시나리오: admin 서비스 헬스 체크
    만약 "admin" 서비스의 "/health"에 요청을 보낸다
    그러면 성공 응답을 받는다

  시나리오: kiosk 서비스 헬스 체크
    만약 "kiosk" 서비스의 "/health"에 요청을 보낸다
    그러면 성공 응답을 받는다

  시나리오: reservation 서비스 헬스 체크
    만약 "reservation" 서비스의 "/health"에 요청을 보낸다
    그러면 성공 응답을 받는다