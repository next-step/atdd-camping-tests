# 테스트 종류

## 테스트 피라미드

```
        /\
       /E2E\        ← 느림, 비용 높음
      /------\
     /Integration\
    /--------------\
   /     Unit       \  ← 빠름, 비용 낮음
  /------------------\
```

## 테스트 종류 비교

| 테스트 | 범위 | 예시 |
|--------|------|------|
| Unit | 클래스/메서드 하나 | `Calculator.add()` 테스트 |
| Integration | 여러 컴포넌트 연결 | Service + Repository + DB |
| E2E | 시스템 전체 (UI → DB) | 브라우저로 로그인 → 주문 → 결제 |
| Acceptance | 비즈니스 요구사항 | "고객이 상품을 주문할 수 있다" |

## 현재 우리 테스트는?

```
테스트 코드 → REST API 호출 → 서비스 컨테이너 → DB
```

- Integration Test 관점: 여러 서비스가 연결되어 동작하는지
- Acceptance Test 관점: Cucumber/Gherkin으로 비즈니스 시나리오 검증
- E2E 관점: UI가 없어서 완전한 E2E는 아님

결론: API 레벨 Acceptance Test (또는 API Integration Test)

## 우리 프로젝트 태그

| 태그 | 용도 | 파일 |
|------|------|------|
| `@smoke` | 환경 정상 확인 | health-check.feature |
| `@api` | API 테스트 | kiosk.feature |