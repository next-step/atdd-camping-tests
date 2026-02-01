# Cucumber

## 기본 설정

### Feature 파일
- `# language: ko` 헤더 추가로 한글 키워드 사용
- Step 패턴 일치 필요 (예: 요청을 보내면 → 요청을 보낸다)

### RunCucumberTest.java
- `@IncludeEngines("cucumber")` 추가
- `@ConfigurationParameter(glue)` 추가 → Step 클래스 위치 지정

## 테스트 제어 기능

| 기능 | 용도 | 예시 |
|------|------|------|
| **Tags** | 시나리오 그룹화/필터링 | `@smoke`, `@api`, `@wip` |
| **Hooks** | 시나리오 전후 실행 | `@Before`, `@After`, `@BeforeAll`, `@AfterAll` |
| **Tagged Hooks** | 특정 태그에만 Hook 적용 | `@Before("@api")` |
| **Order** | Hook 실행 순서 | `@Before(order = 1)` |
| **Filter Tags** | 실행할 태그 지정 | `-Dcucumber.filter.tags="@smoke"` |

### Tags 예시
```gherkin
@smoke
기능: 헬스 체크

@api @slow
기능: 키오스크 상품 조회
```

### Tagged Hooks 예시
```java
@Before("@api")
public void resetDatabase() {
    // @api 시나리오 전에 DB 초기화
}
```

### 실행 필터
```bash
./gradlew test -Dcucumber.filter.tags="@smoke"       # smoke만
./gradlew test -Dcucumber.filter.tags="not @smoke"   # smoke 제외
./gradlew test -Dcucumber.filter.tags="@smoke and @api"
```