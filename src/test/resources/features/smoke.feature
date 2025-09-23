Feature: 모든 인프라가 잘 동작하는지 검증
    Scenario: 키오스크 서비스 동작 확인
        When "http://localhost:8080"에 요청을 보낸다
        Then 성공 응답을 받는다

    Scenario: 관리자 서비스 동작 확인
        When "http://localhost:8081"에 요청을 보낸다
        Then 인증 필요 응답을 받는다

    Scenario: 예약 서비스 동작 확인
        When "http://localhost:8082"에 요청을 보낸다
        Then 성공 응답을 받는다