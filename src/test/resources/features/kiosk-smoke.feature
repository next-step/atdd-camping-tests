# language: ko
  @smoke @kiosk
  기능: Kiosk 서비스 Smoke 테스트

    배경:
      먼저 Kiosk 서비스가 준비될 때까지 대기한다

    시나리오: Kiosk 헬스체크 성공
      만약 Kiosk의 "/" 엔드포인트에 GET 요청을 보낸다
      그러면 응답 상태코드는 200이어야 한다