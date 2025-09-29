@ai-assistant @integration @exception
Feature: 예외 상황 통합 시나리오 테스트

  예외 상황에서 2개 이상의 서비스가 함께 동작하는 시나리오를 검증합니다.

  @ai-assistant @kiosk-admin @service-failure
  Scenario: Admin 서비스 다운 시 Kiosk 동작 처리
    Given 키오스크가 정상적으로 실행 중이다
    When Admin 서비스가 중단된다
    And 키오스크에서 상품 목록을 조회 시도한다
    Then 적절한 오류 메시지가 표시된다
      | expectedMessage |
      | 서비스가 일시적으로 이용 불가능합니다 |
    And 캐시된 상품 정보가 있다면 표시된다
    When 고객이 결제를 시도한다
      | productName | quantity |
      | 캠핑 의자     | 1       |
    Then 결제는 진행되지만 재고 확인이 보류된다
    And 나중에 Admin 서비스 복구 시 재고 동기화가 수행된다

  @ai-assistant @admin-reservation @service-unavailable
  Scenario: Reservation 서비스 다운 시 Admin 예약 관리
    Given 관리자가 로그인되어 있다
    When Reservation 서비스가 중단된다
    And 관리자가 예약 목록을 조회한다
    Then 서비스 연결 오류가 표시된다
      | expectedMessage |
      | 예약 서비스에 연결할 수 없습니다 |
    And 캐시된 예약 정보가 있다면 표시된다
    When 관리자가 예약 상태를 변경 시도한다
      | reservationId | newStatus  |
      | 12345        | CHECKED_IN |
    Then 상태 변경이 큐에 저장된다
    And Reservation 서비스 복구 시 자동으로 동기화된다

  @ai-assistant @kiosk-admin @network-partition
  Scenario: 네트워크 파티션 상황에서 데이터 정합성 처리
    Given 키오스크와 Admin 서비스가 정상 연결되어 있다
    And 상품 재고가 설정되어 있다
      | productName | currentStock |
      | 랜턴         | 5           |
    When 키오스크에서 상품을 판매한다
      | productName | quantity |
      | 랜턴         | 2       |
    And 재고 업데이트 중 네트워크가 단절된다
    Then 키오스크는 판매 확인 메시지를 큐에 저장한다
    When 네트워크가 복구된다
    Then 저장된 판매 확인이 자동으로 Admin에 전송된다
    And Admin 재고가 올바르게 업데이트된다
      | productName | expectedStock |
      | 랜턴         | 3            |

  @ai-assistant @kiosk-admin @auth-failure
  Scenario: JWT 토큰 만료 중 API 호출 처리
    Given 키오스크가 Admin 서비스에 인증되어 있다
    When JWT 토큰이 만료된다
    And 키오스크가 인증이 필요한 API를 호출한다
      | endpoint        | method |
      | /admin/products | GET    |
    Then 401 Unauthorized 오류가 발생한다
    And 키오스크가 자동으로 재인증을 시도한다
    Then 새로운 토큰을 획득한다
    And 원래 API 호출이 재시도되어 성공한다

  @ai-assistant @kiosk-admin @invalid-credentials
  Scenario: 잘못된 인증 정보로 접근 시도
    Given Admin 서비스가 실행 중이다
    When 키오스크가 잘못된 인증 정보로 로그인을 시도한다
      | username | password    |
      | admin   | wrongpasswd |
    Then 인증이 실패한다
    And 적절한 오류 메시지가 표시된다
      | expectedMessage |
      | 인증에 실패했습니다 |
    When 키오스크가 재시도 횟수를 초과한다
    Then 일시적으로 접근이 차단된다
    And 관리자에게 알림이 전송된다

  @ai-assistant @kiosk-admin @unauthorized-access
  Scenario: 권한 부족 상황에서 API 접근
    Given 키오스크가 제한된 권한으로 인증되어 있다
    When 키오스크가 관리자 전용 API를 호출한다
      | endpoint                | method |
      | /admin/users           | GET    |
      | /admin/revenue/report  | GET    |
    Then 403 Forbidden 오류가 발생한다
    And 권한 부족 메시지가 표시된다
      | expectedMessage |
      | 접근 권한이 없습니다 |
    And 보안 로그에 접근 시도가 기록된다

  @ai-assistant @admin-reservation @data-inconsistency
  Scenario: 예약 데이터 불일치 감지 및 복구
    Given Reservation 서비스에 예약이 있다
      | reservationId | status    | customerName |
      | R001         | CONFIRMED | 김고객        |
    And Admin 서비스에 다른 상태의 예약이 있다
      | reservationId | status     | customerName |
      | R001         | CANCELLED  | 김고객        |
    When 데이터 정합성 검증이 실행된다
    Then 불일치가 감지된다
    And 관리자에게 알림이 전송된다
      | alertType        | reservationId |
      | DATA_MISMATCH    | R001         |
    When 관리자가 데이터 동기화를 수행한다
    Then 최신 데이터로 통합된다
    And 동기화 로그가 생성된다

  @ai-assistant @kiosk-admin @inventory-consistency
  Scenario: 재고 데이터 불일치 상황 처리
    Given 키오스크에서 상품 판매가 완료되었다
      | productName | soldQuantity |
      | 텐트         | 3           |
    But Admin 서비스의 재고 업데이트가 실패했다
    When 재고 정합성 검증이 실행된다
    Then 판매 기록과 재고의 불일치가 감지된다
    And 자동 복구가 시도된다
    When 자동 복구가 실패한다
    Then 관리자에게 수동 개입 알림이 전송된다
      | alertType     | productName | issue           |
      | STOCK_MISMATCH| 텐트         | 판매 기록 불일치  |

  @ai-assistant @kiosk-admin @transaction-rollback
  Scenario: 트랜잭션 실패 시 롤백 처리
    Given 고객이 결제를 시작한다
      | productName | quantity | totalAmount |
      | 백팩         | 1       | 80000      |
    When 결제는 성공하지만 재고 업데이트가 실패한다
    Then 전체 트랜잭션이 롤백된다
    And 고객에게 결제 취소 안내가 표시된다
      | expectedMessage |
      | 결제 처리 중 오류가 발생하여 취소되었습니다 |
    And 결제 게이트웨이에 환불 요청이 전송된다
    And Admin 재고는 변경되지 않는다

  @ai-assistant @kiosk-admin @payment-gateway-failure
  Scenario: 외부 결제 게이트웨이 장애 처리
    Given 키오스크에서 상품 구매가 진행 중이다
      | productName | quantity |
      | 쿨러박스     | 1       |
    When 결제 게이트웨이가 응답하지 않는다
    Then 결제 타임아웃이 발생한다
    And 고객에게 적절한 안내 메시지가 표시된다
      | expectedMessage |
      | 결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요. |
    And Admin 서비스의 재고는 변경되지 않는다
    When 고객이 결제를 시작한다
    Then 새로운 결제 세션이 시작된다

  @ai-assistant @kiosk-admin @payment-timeout
  Scenario: 결제 게이트웨이 응답 지연 처리
    Given 고객이 결제를 시작한다
      | productName | quantity | amount |
      | 침낭         | 2       | 60000  |
    When 결제 게이트웨이 응답이 30초를 초과한다
    Then 키오스크가 graceful timeout 처리를 한다
    And 고객에게 진행 상황이 안내된다
      | expectedMessage |
      | 결제 처리 중입니다. 잠시만 기다려주세요. |
    When 응답이 결국 성공으로 돌아온다
    Then 결제가 정상 완료된다
    And Admin 재고가 업데이트된다

  @ai-assistant @kiosk-admin @concurrent-stock-race
  Scenario: 동시 상품 구매 경합 상황
    Given 마지막 재고 1개인 상품이 있다
      | productName | currentStock |
      | 선글라스     | 1           |
    When 여러 키오스크에서 동시에 구매를 시도한다
      | kioskId | productName | quantity | timestamp    |
      | kiosk1  | 선글라스     | 1       | 10:00:00.100 |
      | kiosk2  | 선글라스     | 1       | 10:00:00.105 |
      | kiosk3  | 선글라스     | 1       | 10:00:00.110 |
    Then 하나의 구매만 성공한다
    And 나머지는 재고 부족으로 실패한다
    And 재고가 정확히 0이 된다
    And 동시성 로그가 기록된다

  @ai-assistant @admin-reservation @concurrent-reservation
  Scenario: 동시 예약 생성 경합 처리
    Given 가용한 캠프사이트가 하나 있다
      | siteName | availableDate |
      | Z구역-01  | 2024-12-25   |
    When 여러 고객이 동시에 같은 날짜 예약을 시도한다
      | customerName | checkIn    | checkOut   | timestamp    |
      | 고객A        | 2024-12-25 | 2024-12-27 | 10:00:00.200 |
      | 고객B        | 2024-12-25 | 2024-12-26 | 10:00:00.205 |
    Then 하나의 예약만 성공한다
    And 나머지는 중복 예약 오류로 실패한다
    And 사이트 가용성이 정확하게 업데이트된다
    When 관리자가 예약 현황을 확인한다
    Then 성공한 예약만 표시된다

  @ai-assistant @kiosk-admin @admin-concurrent-modification
  Scenario: 관리자 동시 작업 충돌 해결
    Given 두 명의 관리자가 로그인되어 있다
      | adminId | name  |
      | admin1  | 김관리 |
      | admin2  | 이관리 |
    When 두 관리자가 동시에 같은 상품을 수정한다
      | adminId | productName | newPrice | timestamp    |
      | admin1  | 캠핑테이블    | 45000   | 10:00:00.300 |
      | admin2  | 캠핑테이블    | 50000   | 10:00:00.310 |
    Then 먼저 수정한 것이 적용된다
    And 나중 수정은 충돌 오류가 발생한다
      | expectedMessage |
      | 다른 관리자가 이미 수정했습니다. 새로고침 후 다시 시도하세요. |
    And 충돌 로그가 기록된다
    When 키오스크에서 상품을 조회한다
    Then 올바른 최종 가격이 표시된다
      | productName | expectedPrice |
      | 캠핑테이블    | 45000        |

  @ai-assistant @kiosk-external @payment-gateway-failure
  Scenario: 결제 게이트웨이 장애 처리 (WireMock)
    Given 상품이 준비되어 있다
      | name     | price | stockQuantity |
      | 등산화    | 150000| 3            |
    And 고객이 결제를 시도한다
      | productName | quantity | amount |
      | 등산화       | 1       | 150000 |
    Then 결제 시스템 오류가 발생한다
    And 사용자에게 적절한 안내가 표시된다
      | expectedMessage |
      | 결제 시스템에 일시적 문제가 발생했습니다. 잠시 후 다시 시도해주세요. |
    And 재고는 차감되지 않는다
      | productName | expectedStock |
      | 등산화       | 3            |
    When WireMock 서버가 정상 응답하도록 복구된다
    And 고객이 재시도한다
      | productName | quantity | amount |
      | 등산화       | 1       | 150000 |
    Then 결제가 성공한다
    And 재고가 정상 차감된다

  @ai-assistant @kiosk-external @payment-network-partition
  Scenario: 네트워크 파티션으로 인한 결제 시스템 분리
    Given 고객이 결제를 시작한다
      | productName | quantity | amount |
      | 휴대용가스   | 2       | 8000   |
    When 키오스크와 결제 시스템 간 네트워크가 단절된다
    Then 연결 타임아웃이 발생한다
    And 결제 상태가 불명확하게 된다
    And 임시 대기 상태로 처리된다
      | expectedStatus |
      | PAYMENT_PENDING |
    When 네트워크가 복구된다
    And 결제 상태 확인을 재시도한다
    Then WireMock에서 실제 결제 결과를 확인한다
    And 결제가 성공했다면 재고를 차감한다
    And 결제가 실패했다면 재시도 옵션을 제공한다