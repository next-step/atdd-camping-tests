Feature: 키오스크 결제 E2E (WireMock)

  Scenario: 결제 승인 성공
    When 키오스크에 결제 생성을 요청한다
    And 키오스크에 결제 확정을 요청한다
    Then 결제가 성공이어야 한다

  Scenario: 결제 승인 실패
    When 키오스크에 결제 생성을 요청한다
    And 키오스크에 금액 "12345"원으로 결제 확정을 요청한다
    Then 결제가 실패이어야 한다

