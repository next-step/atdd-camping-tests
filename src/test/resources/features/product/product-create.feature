Feature: 상품 등록 시나리오

  Background:
    Given 관리자 계정으로 로그인이 되어있다

  Scenario: 상품을 등록한다
    When 상품을 등록한다
    Then 상품 등록이 성공한다
    And 등록한 상품이 조회된다
