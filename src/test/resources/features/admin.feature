Feature: admin 서버 컨테이너 스모크 테스트

  Scenario: admin 서버 컨테이너 헬스 체크
    When admin 컨테이너에 요청을 보낸다
    Then 성공 응답을 받는다

  Scenario: 관리자가 상품 목록을 조회할 수 있다
    When 상품 목록 조회를 요청한다
    Then 현재 상품 정보들을 받는다
