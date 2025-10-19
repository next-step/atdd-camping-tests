# language: ko
기능: Smoke Test - 서비스 헬스 체크

  시나리오: Admin 헬스 체크
    만약 "Admin" 서비스의 "/admin"에 요청을 보낸다
    그러면 "Admin" 서비스가 응답한다

  시나리오: Kiosk 헬스 체크
    만약 "Kiosk" 서비스의 "/"에 요청을 보낸다
    그러면 성공 응답을 받는다

  시나리오: Reservation 헬스 체크
    만약 "Reservation" 서비스의 "/"에 요청을 보낸다
    그러면 성공 응답을 받는다
