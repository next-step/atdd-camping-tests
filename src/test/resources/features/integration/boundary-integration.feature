@ai-assistant @integration @boundary
Feature: 경계 조건 통합 시나리오 테스트

  경계 조건에서 2개 이상의 서비스가 함께 동작하는 시나리오를 검증합니다.

  @ai-assistant @kiosk-admin @inventory-boundary
  Scenario: 재고 부족 상황에서 구매 시도
    Given 관리자가 재고가 적은 상품을 등록한다
      | name      | price | stockQuantity | productType |
      | 랜턴       | 15000 | 1            | CAMPING     |
    When 키오스크에서 재고보다 많은 수량을 구매 시도한다
      | productName | attemptQuantity |
      | 랜턴         | 2              |
    Then 구매가 실패한다
    And 적절한 오류 메시지가 표시된다
      | expectedMessage |
      | 재고가 부족합니다 |
    And 관리자 시스템의 재고는 변경되지 않는다
      | productName | expectedStock |
      | 랜턴         | 1            |

  @ai-assistant @kiosk-admin @zero-inventory
  Scenario: 품절 상품 구매 시도 및 재고 음수 방지
    Given 관리자가 품절된 상품을 가지고 있다
      | name    | price | stockQuantity | productType |
      | 백팩     | 80000 | 0            | CAMPING     |
    When 키오스크에서 품절 상품을 구매 시도한다
      | productName | quantity |
      | 백팩         | 1       |
    Then 구매가 거절된다
    And 품절 안내 메시지가 표시된다
      | expectedMessage |
      | 현재 품절된 상품입니다 |
    And 관리자 시스템의 재고가 음수가 되지 않는다
      | productName | expectedStock |
      | 백팩         | 0            |

  @ai-assistant @kiosk-admin @concurrent-purchase
  Scenario: 동시 구매 시 마지막 재고 처리
    Given 관리자가 마지막 재고 1개인 상품을 가지고 있다
      | name     | price | stockQuantity | productType |
      | 버너      | 45000 | 1            | CAMPING     |
    When 두 개의 키오스크에서 동시에 같은 상품을 구매 시도한다
      | kioskId | productName | quantity |
      | kiosk1  | 버너         | 1       |
      | kiosk2  | 버너         | 1       |
    Then 하나의 구매만 성공한다
    And 나머지 구매는 재고 부족으로 실패한다
    And 관리자 시스템의 재고는 0이 된다
      | productName | expectedStock |
      | 버너         | 0            |

  @ai-assistant @admin-reservation @overlapping-dates
  Scenario: 예약 기간 겹침 처리
    Given 기존 예약이 있는 캠프사이트가 있다
      | siteName | existingCheckIn | existingCheckOut | status    |
      | D구역-01  | 2024-12-20     | 2024-12-23      | CONFIRMED |
    When 고객이 겹치는 기간으로 예약을 시도한다
      | siteName | newCheckIn | newCheckOut | guestCount | customerName |
      | D구역-01  | 2024-12-22 | 2024-12-25  | 2         | 김철수        |
    Then 예약이 거절된다
    And 기간 겹침 오류 메시지가 표시된다
      | expectedMessage |
      | 선택한 기간에 이미 예약이 있습니다 |
    When 관리자가 예약 현황을 확인한다
    Then 기존 예약만 유지되고 있다
      | siteName | checkIn    | checkOut   | customerName |
      | D구역-01  | 2024-12-20 | 2024-12-23 | 기존고객      |

  @ai-assistant @admin-reservation @adjacent-dates
  Scenario: 예약 기간 인접 날짜 처리
    Given 기존 예약이 있는 캠프사이트가 있다
      | siteName | existingCheckIn | existingCheckOut | status    |
      | E구역-01  | 2024-12-15     | 2024-12-18      | CONFIRMED |
    When 고객이 바로 다음날부터 예약을 시도한다
      | siteName | newCheckIn | newCheckOut | guestCount | customerName |
      | E구역-01  | 2024-12-18 | 2024-12-21  | 3         | 이영희        |
    Then 예약이 성공한다
    When 관리자가 예약 캘린더를 확인한다
    Then 두 예약이 연속으로 표시된다
      | siteName | period1        | period2        |
      | E구역-01  | 12/15~12/18   | 12/18~12/21    |

  @ai-assistant @admin-reservation @past-date-validation
  Scenario: 과거 날짜 예약 시도 검증
    Given 오늘 날짜가 2024-12-20이다
    When 고객이 과거 날짜로 예약을 시도한다
      | siteName | checkIn    | checkOut   | guestCount | customerName |
      | F구역-01  | 2024-12-18 | 2024-12-19 | 2         | 박민수        |
    Then 예약이 거절된다
    And 과거 날짜 오류 메시지가 표시된다
      | expectedMessage |
      | 과거 날짜로는 예약할 수 없습니다 |
    When 관리자가 예약 로그를 확인한다
    Then 실패한 예약 시도가 기록되어 있다
      | attemptDate | reason      | customerName |
      | 2024-12-20  | PAST_DATE   | 박민수        |

  @ai-assistant @admin-reservation @capacity-boundary
  Scenario: 캠프사이트 최대 수용 인원 초과 검증
    Given 최대 수용 인원이 제한된 캠프사이트가 있다
      | siteName | maxPeople | pricePerNight |
      | G구역-01  | 4        | 30000        |
    When 고객이 최대 인원을 초과하여 예약을 시도한다
      | siteName | checkIn    | checkOut   | guestCount | customerName |
      | G구역-01  | 2024-12-25 | 2024-12-27 | 6         | 최대한        |
    Then 예약이 거절된다
    And 인원 초과 오류 메시지가 표시된다
      | expectedMessage          |
      | 최대 수용 인원을 초과했습니다 |
    When 고객이 적정 인원으로 재시도한다
      | siteName | checkIn    | checkOut   | guestCount | customerName |
      | G구역-01  | 2024-12-25 | 2024-12-27 | 3         | 최대한        |
    Then 예약이 성공한다

  @ai-assistant @admin-reservation @minimum-guest-validation
  Scenario: 예약 최소 인원 미달 처리
    Given 최소 예약 인원이 설정된 캠프사이트가 있다
      | siteName | minPeople | maxPeople | pricePerNight |
      | H구역-01  | 2        | 6        | 40000        |
    When 고객이 최소 인원 미달로 예약을 시도한다
      | siteName | checkIn    | checkOut   | guestCount | customerName |
      | H구역-01  | 2024-12-28 | 2024-12-30 | 1         | 혼자캠핑      |
    Then 예약이 거절되거나 경고가 표시된다
    And 최소 인원 안내 메시지가 표시된다
      | expectedMessage                    |
      | 이 사이트의 최소 이용 인원은 2명입니다 |
    When 관리자가 예약 정책을 확인한다
    Then 사이트별 인원 제한 정책이 올바르게 적용되고 있다

  @ai-assistant @kiosk-admin @product-boundary-update
  Scenario: 상품 수정 시 경계값 처리
    Given 관리자가 기존 상품을 가지고 있다
      | name    | price | stockQuantity | productType |
      | 쿨러    | 120000| 5            | CAMPING     |
    When 관리자가 상품을 경계값으로 수정한다
      | name | newPrice | newStock |
      | 쿨러  | 0       | -1       |
    Then 유효성 검증 오류가 발생한다
    And 적절한 검증 메시지가 표시된다
      | field      | expectedMessage        |
      | price      | 가격은 0보다 커야 합니다    |
      | stockQuantity | 재고는 음수일 수 없습니다 |
    When 키오스크에서 상품 목록을 조회한다
    Then 기존 상품 정보가 유지되고 있다
      | name | expectedPrice | expectedStock |
      | 쿨러  | 120000       | 5            |

  @ai-assistant @kiosk-admin @auth-boundary
  Scenario: 인증 토큰 만료 경계 시점 처리
    Given 키오스크가 관리자 서비스에 인증되어 있다
    And JWT 토큰 만료 시간이 1분 남았다
    When 키오스크가 토큰 만료 직전에 API를 호출한다
      | endpoint        | method |
      | /admin/products | GET    |
    Then API 호출이 성공한다
    When 토큰이 만료된 후 API를 호출한다
      | endpoint        | method |
      | /admin/products | GET    |
    Then 인증 오류가 발생한다
    And 자동으로 재인증을 시도한다
    Then 재인증 후 API 호출이 성공한다

  @ai-assistant @kiosk-external @payment-boundary
  Scenario: 결제 금액 경계값 처리 (WireMock)
    Given WireMock 결제 서버가 실행 중이다
    And 상품이 등록되어 있다
      | name    | price | stockQuantity |
      | 등반용품 | 25000 | 5            |
    When 유효하지 않은 금액으로 결제를 요청한다
    Then 결제가 실패한다
    And 실패 메시지가 "결제 생성 실패"이다
    When 정상 금액으로 결제를 요청한다
    Then 결제가 성공한다
    And 결제 응답에 paymentKey가 포함되어 있다