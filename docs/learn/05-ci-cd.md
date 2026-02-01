# CI/CD

## Smoke 테스트 → E2E 테스트 순차 실행
Smoke 실패 시 E2E 실행하지 않음 (빠른 피드백)

```bash
./gradlew test -Dcucumber.filter.tags="@smoke" && \
./gradlew test -Dcucumber.filter.tags="@api"
```

## GitHub Actions 예시
```yaml
jobs:
  test:
    steps:
      - name: Start Infrastructure
        run: ./gradlew infraUp

      - name: Start Services
        run: ./gradlew servicesUp

      - name: Smoke Test
        run: ./gradlew test -Dcucumber.filter.tags="@smoke"

      - name: E2E Test
        run: ./gradlew test -Dcucumber.filter.tags="@api"

      - name: Stop All
        run: ./gradlew allDown
        if: always()
```