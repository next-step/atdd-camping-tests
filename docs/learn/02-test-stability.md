# 테스트 안정성

## Flaky Test란?
같은 코드인데 성공/실패가 랜덤하게 바뀌는 불안정한 테스트

### 주요 원인
- 서비스가 아직 준비되지 않음
- 네트워크 타이밍 이슈
- 테스트 간 데이터 간섭
- 비동기 처리 완료 전 검증

## 해결 방법 비교

| 방법 | 장점 | 단점 | 추천 상황 |
|------|------|------|----------|
| Sleep | 구현 간단 | 시간 낭비, 여전히 불안정 | 사용 금지 |
| **Polling/Retry** | 필요한 만큼만 대기, 실제 응답 확인 | 의존성 추가 | **E2E 테스트 (권장)** |
| Healthcheck | 컨테이너 레벨에서 보장 | 앱 내부 상태까지는 모름 | 컨테이너 오케스트레이션 |
| Testcontainers | Wait Strategy 내장, 포트 자동 할당 | 라이브러리 의존성 추가 | 테스트에서 컨테이너 직접 관리 |

## Polling/Retry - Awaitility (권장)
Awaitility: 비동기 작업의 완료를 기다리는 Java 라이브러리

```java
@BeforeAll
public static void waitForServices() {
    await().atMost(30, SECONDS)
           .pollInterval(1, SECONDS)
           .until(() -> isServiceReady(url));
}

private static boolean isServiceReady(String url) {
    try {
        return given().get(url).getStatusCode() == 200;
    } catch (Exception e) {
        return false;
    }
}
```

## 테스트 데이터 격리 - DB 초기화
각 시나리오 실행 전 DB 초기화로 테스트 간 간섭 방지

```java
@Before("@api")
public void resetDatabase() {
    try (Connection conn = DriverManager.getConnection(
            TestConfig.getDbUrl(),
            TestConfig.getDbUsername(),
            TestConfig.getDbPassword())) {

        Statement stmt = conn.createStatement();
        stmt.execute("DELETE FROM reservations");
        stmt.execute("DELETE FROM products");

    } catch (SQLException e) {
        throw new RuntimeException("DB 초기화 실패", e);
    }
}
```