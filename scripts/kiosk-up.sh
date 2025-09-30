#!/usr/bin/env bash
set -euo pipefail

# --------------------
# 기본 설정
# --------------------
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT_NAME="atdd-infra"     # compose 프로젝트명 고정
COMPOSE_FILE="$ROOT/infra/docker-compose.yml"
KIOSK_SERVICE="${KIOSK_SERVICE:-kiosk}"  # 로그 대상 서비스명(없으면 무시)

die() { echo "[ERROR] $*" >&2; exit 1; }

# --------------------
# 선행 점검
# --------------------
command -v docker >/dev/null 2>&1 || die "'docker' 명령을 찾을 수 없습니다."
docker compose version >/dev/null 2>&1 || die "'docker compose' 서브커맨드를 사용할 수 없습니다."
docker info >/dev/null 2>&1 || die "Docker 데몬에 연결할 수 없습니다. Docker Desktop 실행 또는 'colima start' 후 재시도하세요."

# gradlew 실행권한 보장
if [[ -f "$ROOT/repos/atdd-camping-kiosk/gradlew" ]]; then
  chmod +x "$ROOT/repos/atdd-camping-kiosk/gradlew" || true
fi

# --------------------
# 1) 소스 동기화
# --------------------
"$ROOT/scripts/clone-kiosk.sh"

# --------------------
# 2) JAR 빌드
# --------------------
(
  cd "$ROOT/repos/atdd-camping-kiosk"
  ./gradlew clean build -x test --warning-mode all
)

# --------------------
# 3) compose up
# --------------------
docker compose \
  -p "$PROJECT_NAME" \
  -f "$COMPOSE_FILE" \
  up -d --build --remove-orphans

# --------------------
# 4) 상태 출력
# --------------------
docker compose -p "$PROJECT_NAME" -f "$COMPOSE_FILE" ps

# 지정 서비스 로그(없으면 무시)
docker compose -p "$PROJECT_NAME" -f "$COMPOSE_FILE" logs "$KIOSK_SERVICE" --tail=100 || true

echo "[OK] kiosk up."
