Feature: 키오스크 결제 플로우 테스트

  Scenario: 키오스크 결제 성공 플로우 (Happy Path)
    When 고객이 키오스크에서 유효한 카드로 결제를 요청한다
    Then 결제가 성공적으로 처리된다
    And 결제 성공 응답이 반환된다

  Scenario: 키오스크 결제 실패 플로우 (Sad Path)
    When 결제 서버가 일시적으로 사용할 수 없는 상태에서 결제를 요청한다
    Then 결제가 실패로 처리된다
    And 결제 실패 응답에 오류 정보가 포함된다

