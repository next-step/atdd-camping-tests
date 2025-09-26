Feature: 키오스크 결제 E2E 테스트

  Background:
    Given 상품 목록에서 결제할 상품을 선택한다
      | productId | quantity | price |
      | 1         | 2        | 5000  |

  Scenario: 결제 성공 - 정상적인 금액으로 결제 요청
    When 정상 금액으로 결제를 요청한다
    Then 결제가 성공한다
    And 결제 응답에 paymentKey가 포함되어 있다
    And 결제 응답에 orderId가 포함되어 있다
    When 결제 승인을 요청한다.
    Then 결제 승인이 성공한다

  Scenario: 결제 실패 - 유효하지 않은 금액 (0원)
    When 유효하지 않은 금액으로 결제를 요청한다
    Then 결제가 실패한다
    And 실패 메시지가 "결제 생성 실패"이다

  Scenario: 결제 실패 - 관리자 결제 승인 실패
    When 정상 금액으로 결제를 요청한다
    Then 결제가 성공한다
    And 결제 응답에 paymentKey가 포함되어 있다
    And 결제 응답에 orderId가 포함되어 있다
    When 존재하지 않는 결제 키로 결제 승인을 요청한다.
    Then 결제 승인이 실패한다
