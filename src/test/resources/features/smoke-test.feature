Feature: Test environment smoke test

  Scenario: 키오스크 서비스 환경이 준비되었는지 확인한다
    When KIOSK 서비스에 요청을 보낸다
    Then 200 응답을 받는다

  Scenario: 어드민 서비스 환경이 준비되었는지 확인한다
    When ADMIN 서비스에 요청을 보낸다
    Then 200 응답을 받는다

  Scenario: 예약 서비스 환경이 준비되었는지 확인한다
    When RESERVATION 서비스에 요청을 보낸다
    Then 200 응답을 받는다
