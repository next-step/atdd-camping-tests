# docker-compose 설정 통일 및 배경 설명

작성일: 2025-09-20 22:04 (로컬)

## 배경
현재 `infra/docker-compose.yml`의 세 서비스(kiosk, admin, reservation)는 `JAVA_OPTS` 설정 방식이 서로 달랐습니다.

- kiosk: `-Dkiosk.base-url=...` 처럼 애플리케이션 고유 속성(`kiosk.base-url`)을 JVM 시스템 속성으로 주입
- admin: `-Dserver.port=8080`
- reservation: `-Dserver.port=8080`

또한 `ADMIN_BASE_URL`, `RESERVATION_BASE_URL` 환경변수가 존재하지만, `JAVA_OPTS`에서는 활용되지 않고 있었습니다.

## 기존 설정의 의미와 이유
- kiosk의 `-Dkiosk.base-url`:
  - 키오스크 애플리케이션은 외부(브라우저, 프록시, 콜백 URL 등)에서 접근 가능한 서비스의 기준 URL이 필요합니다.
  - `kiosk.base-url`은 링크 생성, 리다이렉트, 외부 콜백(예: 결제) 등에서 절대 경로를 계산하기 위한 애플리케이션 레벨 설정입니다.
  - 컨테이너 내부 포트(8080)와는 별개로, **외부에서 접근 가능한 기준 URL**을 명시적으로 전달합니다.

- admin/reservation의 `-Dserver.port=8080`:
  - Spring Boot의 기본 포트가 8080이라 사실상 중복(명시적이긴 하나 기능적 차이는 없음)입니다.
  - docker-compose에서 이미 `"호스트포트:8080"` 형태로 포트 매핑이 이뤄지므로, 컨테이너 내부 포트를 8080로 돌리는 것만으로 충분합니다.
  - 과거 구성에서 "내부 포트를 확실히 고정"하려는 의도였을 수 있으나, 실질적으로는 필요 최소 설정은 아닙니다.

## 통일 원칙
세 서비스 모두 **애플리케이션이 필요로 하는 base-url을 JAVA_OPTS를 통해 명시적으로 주입**하는 방식으로 통일합니다.

- 이유:
  1) 내부 포트는 기본 8080이고, compose의 `ports`로 외부 포트가 매핑되므로 `-Dserver.port=8080`은 중복입니다.
  2) 서비스별 base-url은 외부 접근 경로/도메인/포트를 포함하므로 배포 환경마다 달라질 수 있고, 이를 한 곳에서 명시적으로 관리하는 편이 명확합니다.
  3) 이미 `ADMIN_BASE_URL`, `RESERVATION_BASE_URL` 환경변수가 존재해 패턴을 맞추기 용이합니다.

참고: Spring Boot는 환경변수(`ADMIN_BASE_URL`)를 느슨한 바인딩으로 `admin.base-url`로도 인식할 수 있습니다. 그럼에도 본 리포지토리에서는 전달 경로를 **일관되게 JAVA_OPTS(-D...)**로 맞추는 정책을 사용합니다.

## 변경 사항 요약
`infra/docker-compose.yml`에서 다음과 같이 변경했습니다.

- kiosk: (변경 없음)
  - `JAVA_OPTS=-Dkiosk.base-url=${KIOSK_BASE_URL:-http://localhost:${KIOSK_PORT:-18081}}`

- admin: (변경함)
  - 이전: `JAVA_OPTS=-Dserver.port=8080`
  - 변경: `JAVA_OPTS=-Dadmin.base-url=${ADMIN_BASE_URL:-http://localhost:${ADMIN_PORT:-18082}}`

- reservation: (변경함)
  - 이전: `JAVA_OPTS=-Dserver.port=8080`
  - 변경: `JAVA_OPTS=-Dreservation.base-url=${RESERVATION_BASE_URL:-http://localhost:${RESERVATION_PORT:-18083}}`

해당 변경으로 세 서비스 모두 `BASE_URL` 환경변수를 기준으로 애플리케이션 속성(`*.base-url`)을 JVM 시스템 속성으로 주입하는 한 가지 방식으로 통일되었습니다.

## 최종 docker-compose 발췌
- admin
```
environment:
  - ADMIN_PORT=${ADMIN_PORT:-18082}
  - ADMIN_BASE_URL=${ADMIN_BASE_URL:-http://localhost:${ADMIN_PORT:-18082}}
  - JAVA_OPTS=-Dadmin.base-url=${ADMIN_BASE_URL:-http://localhost:${ADMIN_PORT:-18082}}
```

- reservation
```
environment:
  - RESERVATION_PORT=${RESERVATION_PORT:-18083}
  - RESERVATION_BASE_URL=${RESERVATION_BASE_URL:-http://localhost:${RESERVATION_PORT:-18083}}
  - JAVA_OPTS=-Dreservation.base-url=${RESERVATION_BASE_URL:-http://localhost:${RESERVATION_PORT:-18083}}
```

- kiosk (참고)
```
environment:
  - KIOSK_PORT=${KIOSK_PORT:-18081}
  - KIOSK_BASE_URL=${KIOSK_BASE_URL:-http://localhost:${KIOSK_PORT:-18081}}
  - JAVA_OPTS=-Dkiosk.base-url=${KIOSK_BASE_URL:-http://localhost:${KIOSK_PORT:-18081}}
```

## 사용/오버라이드 방법
- 로컬에서 포트만 바꾸고 싶다면(예: admin):
  - `ADMIN_PORT=28082 docker compose up -d` (외부 포트만 변경, 내부는 8080 유지)
- base-url을 도메인으로 바꾸고 싶다면:
  - `ADMIN_BASE_URL=https://admin.example.com docker compose up -d`
- 필요 시 포트와 base-url 둘 다 지정 가능:
  - `ADMIN_PORT=28082 ADMIN_BASE_URL=http://localhost:28082 docker compose up -d`

## 주의사항
- 애플리케이션 코드 내에서 `admin.base-url`, `reservation.base-url`, `kiosk.base-url`을 실제로 사용하는지 확인하세요. 본 통일안은 설정 전달 경로를 합리적으로 맞춘 것이며, 사용하는 지점은 각 서비스의 구현에 의존합니다.
- 리버스 프록시(Nginx 등) 뒤에 둘 경우, base-url은 외부에서 접근 가능한 스킴/호스트/포트를 반영해야 합니다.

## 결론
- 중복되고 불필요한 `-Dserver.port=8080` 대신, 각 서비스의 외부 기준 경로를 나타내는 `*.base-url`을 `JAVA_OPTS`로 주입하는 방식으로 통일했습니다.
- 이로써 환경 간 이식성(로컬/스테이징/프로덕션) 및 설정 가시성이 개선됩니다.
