# language: ko
기능: 스모크 테스트 - 각 서비스 200 응답 확인

#  시나리오: Admin 서비스 헬스 체크
#    만약 "http://localhost:18080"에 요청을 보낸다
#    그러면 성공 응답을 받는다

  시나리오: Kiosk 서비스 헬스 체크
    만약 "http://localhost:18081"에 요청을 보낸다
    그러면 성공 응답을 받는다

#  시나리오: Reservation 서비스 헬스 체크
#    만약 "http://localhost:18082"에 요청을 보낸다
#    그러면 성공 응답을 받는다


