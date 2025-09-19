# language: ko
기능: 외부 프로세스로 기동한 키오스크에 대한 스모크 테스트

  # KIOSK_BASE_URL 환경변수/시스템 프로퍼티로 베이스 URL을 주입받는다 (예: http://localhost:18081)
  시나리오: 헬스 체크
    만약 "http://localhost:8080"에 요청을 보낸다
    그러면 성공 응답을 받는다


