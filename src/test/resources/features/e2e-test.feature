Feature: 서비스 간 통합 테스트

  Scenario: Admin 로그인 후 Kiosk 상품 목록 조회
    When Admin 로그인 API "/auth/login"에 관리자 계정으로 로그인 요청을 보낸다
    Then 200 응답을 받는다
    And 인증 토큰 또는 쿠키를 받는다
    When Kiosk 상품 목록 API "/api/products"에 요청을 보낸다
    Then 200 응답을 받는다
    And 응답 배열의 길이가 1 이상이다
    And 상품 정보의 주요 필드들이 존재한다
