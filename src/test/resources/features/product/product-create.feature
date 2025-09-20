Feature: 상품 등록 시나리오

  Background:
    Given 관리자 계정으로 로그인이 되어있다

  Scenario: 상품을 등록한다
    When 어드민에서 '텐트' 상품을 등록한다
    Then 어드민에서 상품 등록이 성공한다
    And 키오스크에서 '텐트' 상품이 조회된다
