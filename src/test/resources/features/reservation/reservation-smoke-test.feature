Feature: reservation 서버 컨테이너 스모크 테스트

  Scenario: reservation 서버 컨테이너 헬스 체크
    When reservation 컨테이너에 요청을 보낸다
    Then 성공 응답을 받는다


