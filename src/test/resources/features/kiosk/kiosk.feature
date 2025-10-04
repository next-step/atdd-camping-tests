Feature: Kiosk 서비스 상품 조회 기능

  Scenario: kiosk 서비스에서 전체 상품 현황을 조회한다
    When 키오스크에서 전체 상품 조회를 요청한다
    Then 성공 응답을 받는다
    And 1개 이상의 상품 정보가 확인된다
