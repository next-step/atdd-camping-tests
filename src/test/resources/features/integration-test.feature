Feature: 서비스 간 통합 테스트
  Admin과 Kiosk 서비스 간의 연동을 통한 통합 테스트를 수행한다.
  Admin에서 인증을 받고, Kiosk에서 상품 목록을 조회하는 시나리오를 검증한다.

  Scenario: Admin 로그인 후 Kiosk 상품 목록 조회
    Given 관리자 시스템에서 토큰을 받았다
    When 키오스크 상품 목록을 조회한다
    Then 200 응답을 받는다
    And 응답 배열의 길이가 1 이상이다
    And 상품 정보의 주요 필드들이 존재한다
