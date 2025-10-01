# ATDD Kiosk Smoke Test

이 문서는 **kiosk 서비스의 컨테이너 기동 및 스모크 테스트(200 응답 확인)** 방법을 안내합니다.  
모든 과정은 스크립트로 자동화되어 있으며, 이 문서만 따라 하면 누구나 재현할 수 있습니다.

---

## ✅ 사전 준비
- Docker / Docker Compose 설치
- JDK 17 이상
- Gradle

---

## 🚀 실행 절차

### 1. kiosk 컨테이너 기동
``` bash
./scripts/kiosk-up.sh
```
atdd-tests/infra/docker-compose.yml을 기반으로 kiosk 컨테이너가 기동됩니다.

호스트 포트 18081 → 컨테이너 포트 8080 매핑

2. 스모크 테스트 실행

``` bash
./scripts/test-smoke.sh
```
Rest Assured + Awaitility 기반의 테스트가 실행됩니다.

엔드포인트(/, /actuator/health)를 호출하여 HTTP 200 OK 확인

최대 60초 동안 1초 간격으로 재시도하여 컨테이너 준비 지연도 커버

3. kiosk 컨테이너 종료
``` bash 
./scripts/kiosk-down.sh
```
 환경 변수
`KIOSK_BASE_URL`

테스트 대상 베이스 URL (기본값: http://localhost:18081)

`KIOSK_HEALTH_PATH`

엔드포인트 경로 (기본값: /actuator/health)

예시:

```bash
export KIOSK_BASE_URL=http://localhost:18081
export KIOSK_HEALTH_PATH=/
./scripts/test-smoke.sh
```
