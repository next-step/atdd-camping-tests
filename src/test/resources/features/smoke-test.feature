Feature: Test environment smoke test

  Scenario: 서비스에 요청을 보낼 수 있는지 확인한다
    When KIOSK 서비스에 요청을 보낸다
    Then 200 응답을 받는다
    When ADMIN 서비스에 요청을 보낸다
    Then 200 응답을 받는다
    When RESERVATION 서비스에 요청을 보낸다
    Then 200 응답을 받는다
