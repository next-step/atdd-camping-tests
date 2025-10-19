Feature: admin 서비스 상품 관리 기능

  Scenario: 관리자가 상품 목록을 조회할 수 있다
    When 상품 목록 조회를 요청한다
    Then 현재 상품 정보들을 받는다
