# language: ko
  @smoke
  기능: 서비스 Smoke 테스트

    시나리오: Kiosk 헬스체크
      만약 Kiosk의 "/" 엔드포인트에 GET 요청을 보낸다
      그러면 응답 상태코드는 200이어야 한다

    시나리오: Admin 헬스체크
      만약 Admin의 "/" 엔드포인트에 GET 요청을 보낸다
      그러면 응답 상태코드는 401이어야 한다

    시나리오: Reservation의 헬스체크
      만약 Reservation의 "/" 엔드포인트에 GET 요청을 보낸다
      그러면 응답 상태코드는 200이어야 한다