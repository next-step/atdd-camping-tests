Feature: Test environment smoke test
  Scenario: 서비스에 요청을 보낼 수 있는지 확인한다
    When kiosk 서비스에 요청을 보낸다
    Then 200 응답을 받는다
