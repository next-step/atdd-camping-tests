# language: ko
기능: 캠핑 시스템 스모크 테스트

  시나리오: Kiosk 서비스 연결 테스트
    만약 "http://localhost:18081/"에 요청을 보낸다
    그러면 200 응답을 받는다

  시나리오: Admin 서비스 연결 테스트
    만약 "http://localhost:18082/login"에 요청을 보낸다
    그러면 200 응답을 받는다

  시나리오: Reservation 서비스 연결 테스트
    만약 "http://localhost:18083/"에 요청을 보낸다
    그러면 200 응답을 받는다