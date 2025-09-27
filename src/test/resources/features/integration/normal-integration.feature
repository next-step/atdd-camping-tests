@ai-assistant @integration @normal
Feature: 정상 통합 시나리오 테스트

  정상적인 비즈니스 플로우에서 2개 이상의 서비스가 함께 동작하는 시나리오를 검증합니다.

  @ai-assistant @kiosk-admin @product-lifecycle
  Scenario: 상품 생성부터 판매까지 전체 통합 플로우
    Given 관리자가 로그인되어 있다
    When 관리자가 새로운 상품을 등록한다
      | name        | price | stockQuantity | productType |
      | 캠핑 텐트      | 50000 | 10           | CAMPING     |
    Then 상품이 성공적으로 등록된다
    When 키오스크에서 상품 목록을 조회한다
    Then 등록한 상품이 키오스크에 표시된다
    When 고객이 키오스크에서 상품을 구매한다
      | productName | quantity |
      | 캠핑 텐트      | 2        |
    Then 결제가 성공한다
    And 관리자 시스템의 재고가 올바르게 차감된다
      | productName | expectedStock |
      | 캠핑 텐트      | 8            |
    And 매출 기록이 생성된다

  @ai-assistant @kiosk-admin @product-sync
  Scenario: 상품 정보 수정 시 키오스크 실시간 반영
    Given 관리자가 로그인되어 있다
    And 상품이 이미 등록되어 있다
      | name     | price | stockQuantity | productType |
      | 슬리핑백   | 30000 | 5            | CAMPING     |
    When 관리자가 상품 정보를 수정한다
      | name     | newPrice | newStock |
      | 슬리핑백   | 35000   | 8        |
    Then 상품 정보가 성공적으로 수정된다
    When 키오스크에서 상품 목록을 다시 조회한다
    Then 수정된 상품 정보가 키오스크에 반영된다
      | name     | expectedPrice | expectedStock |
      | 슬리핑백   | 35000        | 8            |

  @ai-assistant @admin-reservation @reservation-management
  Scenario: 예약 생성부터 관리까지 전체 플로우
    Given 예약 가능한 캠프사이트가 있다
      | siteName | maxPeople | pricePerNight |
      | A구역-01  | 4        | 25000        |
    When 고객이 예약을 생성한다
      | siteName | checkIn    | checkOut   | guestCount | customerName | phone        |
      | A구역-01  | 2024-12-25 | 2024-12-27 | 3         | 홍길동        | 010-1234-5678 |
    Then 예약이 성공적으로 생성된다
    When 관리자가 예약 목록을 조회한다
    Then 생성된 예약이 관리자 시스템에 표시된다
      | customerName | siteName | status    |
      | 홍길동        | A구역-01  | CONFIRMED |
    When 관리자가 예약 상태를 변경한다
      | customerName | newStatus  |
      | 홍길동        | CHECKED_IN |
    Then 예약 상태가 성공적으로 변경된다

  @ai-assistant @admin-reservation @site-availability
  Scenario: 캠프사이트 가용성 확인 및 예약 처리
    Given 캠프사이트의 예약 현황이 있다
      | siteName | reservedDates        | status    |
      | B구역-01  | 2024-12-20~2024-12-22 | CONFIRMED |
    When 고객이 사이트 가용성을 확인한다
      | siteName | checkDate  |
      | B구역-01  | 2024-12-21 |
    Then 해당 날짜는 예약 불가능으로 표시된다
    When 고객이 다른 날짜의 가용성을 확인한다
      | siteName | checkDate  |
      | B구역-01  | 2024-12-24 |
    Then 해당 날짜는 예약 가능으로 표시된다
    When 관리자가 전체 캠프사이트 현황을 조회한다
    Then 각 사이트의 예약 상태가 정확하게 표시된다

  @ai-assistant @kiosk-admin @auth-flow
  Scenario: 키오스크 인증 및 권한 관리 플로우
    Given 관리자 서비스가 실행 중이다
    When 키오스크가 관리자 서비스에 인증을 요청한다
      | username | password |
      | admin   | admin123 |
    Then 인증이 성공하고 JWT 토큰을 받는다
    When 키오스크가 인증이 필요한 API를 호출한다
      | endpoint        | method |
      | /admin/products | GET    |
    Then API 호출이 성공한다
    And 올바른 인증 헤더가 포함되어 있다

  @ai-assistant @kiosk-admin @inventory-sync
  Scenario: 재고 관리 연동 시나리오
    Given 관리자가 상품 재고를 설정한다
      | productName | initialStock |
      | 캠핑 의자     | 15          |
    When 키오스크에서 해당 상품을 조회한다
    Then 올바른 재고 수량이 표시된다
      | productName | expectedStock |
      | 캠핑 의자     | 15           |
    When 키오스크에서 상품을 판매한다
      | productName | soldQuantity |
      | 캠핑 의자     | 3           |
    Then 관리자 시스템의 재고가 자동으로 업데이트된다
      | productName | expectedStock |
      | 캠핑 의자     | 12           |
    And 매출 통계에 판매 내역이 반영된다
      | productName | soldAmount | soldQuantity |
      | 캠핑 의자     | 75000     | 3           |

  @ai-assistant @admin-reservation @reservation-calendar
  Scenario: 예약 캘린더 연동 및 가용성 관리
    Given 여러 개의 예약이 존재한다
      | siteName | checkIn    | checkOut   | status    |
      | C구역-01  | 2024-12-15 | 2024-12-17 | CONFIRMED |
      | C구역-02  | 2024-12-16 | 2024-12-18 | CONFIRMED |
      | C구역-01  | 2024-12-20 | 2024-12-22 | PENDING   |
    When 관리자가 월별 예약 캘린더를 조회한다
      | year | month |
      | 2024 | 12    |
    Then 각 날짜별 사이트 가용성이 정확하게 표시된다
    And 예약 상태별로 구분되어 표시된다
    When 예약 서비스에서 새로운 예약이 생성된다
      | siteName | checkIn    | checkOut   |
      | C구역-03  | 2024-12-25 | 2024-12-27 |
    Then 관리자 캘린더가 실시간으로 업데이트된다